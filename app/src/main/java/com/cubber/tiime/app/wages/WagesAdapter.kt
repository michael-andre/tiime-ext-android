package com.cubber.tiime.app.wages

import android.arch.paging.PagedList
import android.content.Context
import android.databinding.OnRebindCallback
import android.graphics.drawable.Drawable
import android.support.transition.TransitionManager
import android.support.v7.widget.RecyclerView
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cubber.tiime.R
import com.cubber.tiime.databinding.WageItemBinding
import com.cubber.tiime.model.Holiday
import com.cubber.tiime.model.Wage
import com.cubber.tiime.utils.holidayTypeIndicator
import com.cubber.tiime.utils.selectableItemBackgroundBorderless
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.format.DayFormatter
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter
import com.wapplix.recycler.BindingPagedListAdapter
import java.util.*

/**
 * Created by mike on 21/09/17.
 */
class WagesAdapter(
        context: Context,
        private val dateSelectedListener: OnDateSelectedListener
) : BindingPagedListAdapter<Wage, WageItemBinding>({ id }) {

    private var showYearView: Boolean = false
    private var expandedIds: MutableSet<Long>? = null

    private val holidayDecorators = HashMap<String, HolidayDecorator>()
    private val commonDayDecorator = object : DayViewDecorator {

        private val background = selectableItemBackgroundBorderless(context)!!

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return true
        }

        override fun decorate(view: DayViewFacade) {
            view.setBackgroundDrawable(background)
        }

    }
    private val weekDayFormatter = WeekDayFormatter { dayOfWeek ->
        if (showYearView)
            DateUtils.getDayOfWeekString(dayOfWeek, DateUtils.LENGTH_SHORTEST)
        else
            WeekDayFormatter.DEFAULT.format(dayOfWeek)
    }
    private val dayFormatter = DayFormatter { day -> if (showYearView) "•" else DayFormatter.DEFAULT.format(day) }

    init {
        setHasStableIds(true)
        for (type in Holiday.TYPES) {
            holidayDecorators.put(type, HolidayDecorator(holidayTypeIndicator(context, type)!!))
        }
    }

    override fun setList(pagedList: PagedList<Wage>?) {
        super.setList(pagedList)
        // Reset all days in decorators
        holidayDecorators.forEach { it.value.days.clear() }
        // Populate decorators with holidays
        if (pagedList == null) return
        val cal = Calendar.getInstance()
        for (wage in pagedList) {
            for (holiday in wage.holidays.orEmpty()) {
                val d = holidayDecorators[holiday.type] ?: continue
                d.days.add(CalendarDay.from(holiday.startDate))
                if (holiday.duration > 2) {
                    cal.time = holiday.startDate
                    for (offset in 2..(holiday.duration / 2)) {
                        cal.add(Calendar.DAY_OF_MONTH, 1)
                        d.days.add(CalendarDay.from(cal))
                    }
                }
            }
        }
        // Expand editable months
        if (expandedIds == null) {
            expandedIds = pagedList.asSequence()
                    .filter { it.editable }
                    .map { it.id }
                    .toMutableSet()
        }
    }

    fun setShowYearView(displayYear: Boolean) {
        showYearView = displayYear
        notifyDataSetChanged()
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): WageItemBinding {
        val binding = WageItemBinding.inflate(inflater, parent, false)
        binding.calendar.topbarVisible = false
        binding.calendar.isPagingEnabled = false
        binding.calendar.isDynamicHeightEnabled = true
        binding.calendar.selectionMode = MaterialCalendarView.SELECTION_MODE_SINGLE
        binding.calendar.addDecorator(commonDayDecorator)
        binding.calendar.addDecorators(holidayDecorators.values)
        binding.calendar.setOnDateChangedListener(dateSelectedListener)
        binding.toolbar.inflateMenu(R.menu.wage_context)
        binding.calendar.setWeekDayFormatter(weekDayFormatter)
        binding.calendar.setDayFormatter(dayFormatter)
        binding.calendar.visibility = View.GONE
        binding.addOnRebindCallback(object : OnRebindCallback<WageItemBinding>() {

            private val increaseBonusItem = binding.toolbar.menu.findItem(R.id.increase_bonus)
            private val commentItem = binding.toolbar.menu.findItem(R.id.comment)

            override fun onBound(binding: WageItemBinding?) {
                with(binding!!.wage!!) {
                    val showMenu = !showYearView && editable
                    increaseBonusItem.isVisible = showMenu
                    commentItem.isVisible = showMenu
                }
            }

        })
        return binding
    }

    override fun onBindView(binding: WageItemBinding, item: Wage) {
        binding.wage = item
        binding.displayYear = showYearView
        binding.holidaysSummary = Wages.getHolidaysSummary(item)
        binding.calendar.visibility = if (showYearView || expandedIds!!.contains(item.id)) View.VISIBLE else View.GONE
        with(binding.calendar.state().edit()) {
            val cal = Calendar.getInstance()
            cal.time = item.period
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH))
            setMinimumDate(cal)
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
            setMaximumDate(cal)
            commit()
        }
        binding.holidaysRow.setOnClickListener {
            TransitionManager.beginDelayedTransition(binding.root.parent as ViewGroup)
            if (expandedIds!!.remove(item.id)) {
                binding.calendar.visibility = View.GONE
            } else {
                expandedIds!!.add(item.id)
                binding.calendar.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemId(position: Int): Long {
        val e = getItem(position)
        return e?.id ?: RecyclerView.NO_ID
    }

    private class HolidayDecorator(
            val drawable: Drawable
    ) : DayViewDecorator {

        val days = HashSet<CalendarDay>()

        override fun shouldDecorate(day: CalendarDay): Boolean {
            return days.contains(day)
        }

        override fun decorate(view: DayViewFacade) {
            view.setBackgroundDrawable(drawable)
        }

    }

}
