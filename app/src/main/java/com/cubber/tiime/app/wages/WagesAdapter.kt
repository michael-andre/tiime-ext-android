package com.cubber.tiime.app.wages

import android.arch.paging.PagedList
import android.content.Context
import android.databinding.OnRebindCallback
import android.graphics.drawable.Drawable
import android.support.transition.TransitionManager
import android.support.v4.util.LongSparseArray
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
        private val expandedIds: LongSparseArray<Boolean>,
        private val listener: Listener
) : BindingPagedListAdapter<Wage, WageItemBinding>({ id }) {

    interface Listener {
        fun onDateSelected(date: Date)
        fun onEditComment(item: Wage)
        fun onEditIncreaseBonus(item: Wage)
    }

    private var viewYear: Boolean = false

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
        if (viewYear)
            DateUtils.getDayOfWeekString(dayOfWeek, DateUtils.LENGTH_SHORTEST)
        else
            WeekDayFormatter.DEFAULT.format(dayOfWeek)
    }
    private val dayFormatter = DayFormatter { day -> if (viewYear) "•" else DayFormatter.DEFAULT.format(day) }
    private val dateListener = OnDateSelectedListener { view, d, _ ->
        view.setDateSelected(d, false)
        listener.onDateSelected(d.date)
    }

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
    }

    fun setViewYear(displayYear: Boolean) {
        viewYear = displayYear
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
        binding.calendar.setOnDateChangedListener(dateListener)
        binding.calendar.setWeekDayFormatter(weekDayFormatter)
        binding.calendar.setDayFormatter(dayFormatter)
        binding.toolbar.inflateMenu(R.menu.wage_context)
        binding.addOnRebindCallback(object : OnRebindCallback<WageItemBinding>() {

            private val increaseBonusItem = binding.toolbar.menu.findItem(R.id.increase_bonus)
            private val commentItem = binding.toolbar.menu.findItem(R.id.comment)

            override fun onBound(binding: WageItemBinding?) {
                with(binding!!.wage!!) {
                    val showMenu = !viewYear && editable
                    increaseBonusItem.isVisible = showMenu
                    commentItem.isVisible = showMenu
                }
            }

        })
        return binding
    }

    override fun onBindView(binding: WageItemBinding, item: Wage) {
        binding.wage = item
        binding.displayYear = viewYear
        binding.holidaysSummary = Wages.getHolidaysSummary(item)
        binding.calendar.visibility = if (viewYear || shouldExpand(item)) View.VISIBLE else View.GONE
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
            binding.calendar.visibility = if (shouldExpand(item)) {
                expandedIds.put(item.id, false)
                View.GONE
            } else {
                expandedIds.put(item.id, true)
                View.VISIBLE
            }
        }
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.comment -> {
                    listener.onEditComment(item)
                    true
                }
                R.id.increase_bonus -> {
                    listener.onEditIncreaseBonus(item)
                    true
                }
                else -> false
            }
        }
        binding.commentRow.setOnClickListener { listener.onEditComment(item) }
        binding.increaseBonusRow.setOnClickListener { listener.onEditIncreaseBonus(item) }
    }

    private fun shouldExpand(item: Wage): Boolean {
        var expanded = expandedIds.get(item.id)
        if (expanded == null) {
            expanded = item.editable
            expandedIds.put(item.id, expanded)
        }
        return expanded
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: RecyclerView.NO_ID
    }

    private class HolidayDecorator(
            private val drawable: Drawable
    ) : DayViewDecorator {

        val days = mutableSetOf<CalendarDay>()

        override fun shouldDecorate(day: CalendarDay): Boolean = days.contains(day)

        override fun decorate(view: DayViewFacade) {
            view.setBackgroundDrawable(drawable)
        }

    }

}
