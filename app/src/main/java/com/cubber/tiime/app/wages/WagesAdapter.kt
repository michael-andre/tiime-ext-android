package com.cubber.tiime.app.wages

import android.arch.paging.PagedList
import android.content.Context
import android.databinding.OnRebindCallback
import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.RippleDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.ArcShape
import android.graphics.drawable.shapes.OvalShape
import android.os.Build
import android.support.transition.TransitionManager
import android.support.v4.content.ContextCompat
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
import com.cubber.tiime.utils.holidayTypeColor
import com.prolificinteractive.materialcalendarview.*
import com.prolificinteractive.materialcalendarview.format.DayFormatter
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter
import com.wapplix.arch.paging.diffCallbackBy
import com.wapplix.recycler.BindingPagedListAdapter
import java.util.*
import kotlin.math.ceil

/**
 * Created by mike on 21/09/17.
 */
class WagesAdapter(
        context: Context,
        private val expandedIds: LongSparseArray<Boolean>,
        private val listener: Listener
) : BindingPagedListAdapter<Wage, WageItemBinding>(diffCallbackBy { id }) {

    interface Listener {
        fun onDateSelected(date: Date, days: Int)
        fun onEditComment(item: Wage)
        fun onEditIncreaseBonus(item: Wage)
        fun onViewAttachment(item: Wage)
    }

    private val holidayDecorators = mutableMapOf<DecoratorKey, HolidayDecorator>()

    private val weekDayFormatter = WeekDayFormatter { dayOfWeek -> DateUtils.getDayOfWeekString(dayOfWeek, DateUtils.LENGTH_SHORTEST) }
    private val dayFormatter = DayFormatter { day -> DayFormatter.DEFAULT.format(day) }

    private val dateListener = OnDateSelectedListener { view, d, _ ->
        listener.onDateSelected(d.date, 1)
        view.clearSelection()
    }
    private val rangeListener = OnRangeSelectedListener { view, d ->
        listener.onDateSelected(d[0].date, d.size)
        view.clearSelection()
    }

    init {
        setHasStableIds(true)
        initHolidayDecorators(context)
    }

    override fun submitList(pagedList: PagedList<Wage>?) {
        super.submitList(pagedList)
        // Reset all days in decorators
        holidayDecorators.forEach { it.value.days.clear() }
        // Populate decorators with holidays
        if (pagedList == null) return
        val cal = Calendar.getInstance()
        for (wage in pagedList) {
            for (holiday in wage.holidays.orEmpty()) {
                val type = if (Holiday.TYPES.contains(holiday.type)) holiday.type else null
                val d = holidayDecorators[DecoratorKey(type, holiday.duration == 1, Wages.isEditable(wage))]!!
                d.days.add(CalendarDay.from(holiday.startDate))
                if (holiday.duration > 2) {
                    cal.time = holiday.startDate
                    for (offset in 2..ceil(holiday.duration / 2f).toInt()) {
                        cal.add(Calendar.DAY_OF_MONTH, 1)
                        d.days.add(CalendarDay.from(cal))
                    }
                }
            }
        }
    }

    override fun onCreateViewBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): WageItemBinding {
        return WageItemBinding.inflate(inflater, parent, false).apply {
            calendar.topbarVisible = false
            calendar.isPagingEnabled = false
            calendar.firstDayOfWeek
            //calendar.isDynamicHeightEnabled = true
            calendar.selectionMode = MaterialCalendarView.SELECTION_MODE_SINGLE
            calendar.addDecorators(holidayDecorators.values)
            calendar.setOnDateChangedListener(dateListener)
            calendar.setOnRangeSelectedListener(rangeListener)
            calendar.setWeekDayFormatter(weekDayFormatter)
            calendar.setDayFormatter(dayFormatter)
            toolbar.inflateMenu(R.menu.wage_context)
            addOnRebindCallback(object : OnRebindCallback<WageItemBinding>() {

                private val increaseBonusItem by lazy { toolbar.menu.findItem(R.id.increase_bonus) }
                private val commentItem by lazy { toolbar.menu.findItem(R.id.comment) }

                override fun onBound(binding: WageItemBinding) {
                    binding.wage?.apply {
                        increaseBonusItem.isVisible = editable
                        commentItem.isVisible = editable
                    }
                }

            })
        }
    }

    override fun onBindView(binding: WageItemBinding, item: Wage) {
        with(binding) {
            wage = item
            editable = Wages.isEditable(item)
            validationRequired = item.status == Wage.STATUS_VALIDATION_REQUIRED
            holidaysSummary = Wages.getHolidaysSummary(item)
            calendar.visibility = if (shouldExpand(item)) View.VISIBLE else View.GONE
            with(calendar.state().edit()) {
                val cal = Calendar.getInstance()
                cal.firstDayOfWeek
                cal.time = item.period
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH))
                // Only allow dynamic sizing when the first day of the month is on week 1 (bug)
                calendar.isDynamicHeightEnabled = cal.get(Calendar.WEEK_OF_MONTH) == 1
                setMinimumDate(cal)
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
                setMaximumDate(cal)
                commit()
            }
            calendar.invalidateDecorators()
            holidaysRow.setOnClickListener {
                TransitionManager.beginDelayedTransition(root.parent as ViewGroup)
                val expanded = calendar.visibility != View.VISIBLE
                expandedIds.put(item.id, false)
                calendar.visibility = if (expanded) View.VISIBLE else View.GONE
            }
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.comment -> {
                        listener.onEditComment(item); true
                    }
                    R.id.increase_bonus -> {
                        listener.onEditIncreaseBonus(item); true
                    }
                    else -> false
                }
            }
            commentRow.setOnClickListener { listener.onEditComment(item) }
            attachmentRow.setOnClickListener { listener.onViewAttachment(item) }
            increaseBonusRow.setOnClickListener { listener.onEditIncreaseBonus(item) }
        }
    }

    private fun shouldExpand(item: Wage): Boolean {
        var expanded = expandedIds.get(item.id)
        if (expanded == null) {
            expanded = Wages.isEditable(item)
            expandedIds.put(item.id, expanded)
        }
        return expanded
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: RecyclerView.NO_ID
    }

    private fun initHolidayDecorators(context: Context) {
        for (type in Holiday.TYPES) {
            initHolidayDecorators(context, type)
        }
        initHolidayDecorators(context, null)
    }

    private fun initHolidayDecorators(context: Context, @Holiday.Type type: String?) {
        initHolidayDecorator(context, type, true, true)
        initHolidayDecorator(context, type, true, false)
        initHolidayDecorator(context, type, false, true)
        initHolidayDecorator(context, type, false, false)
    }

    private fun initHolidayDecorator(context: Context, @Holiday.Type type: String?, halfDay: Boolean, editable: Boolean) {
        val key = DecoratorKey(type, halfDay, editable)
        holidayDecorators[key] = HolidayDecorator(context, key)
    }

    private data class DecoratorKey(
            @Holiday.Type val type: String?,
            val halfDay: Boolean,
            val editable: Boolean
    )

    private class HolidayDecorator(context: Context, key: DecoratorKey) : DayViewDecorator {

        val days = mutableSetOf<CalendarDay>()
        val drawable: Drawable

        init {
            val shapeDrawable = ShapeDrawable(if (key.halfDay) ArcShape(-90f, -180f) else OvalShape())
            shapeDrawable.paint.color = holidayTypeColor(context, key.type)
            shapeDrawable.paint.alpha = if (key.editable) 255 else 127
            val inset = context.resources.getDimensionPixelSize(R.dimen.holiday_background_inset)
            drawable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                InsetDrawable(RippleDrawable(ContextCompat.getColorStateList(context, R.color.holiday_indicator_ripple), shapeDrawable, shapeDrawable), inset)
            } else {
                InsetDrawable(shapeDrawable, inset)
            }
        }

        override fun shouldDecorate(day: CalendarDay): Boolean = days.contains(day)

        override fun decorate(view: DayViewFacade) = view.setBackgroundDrawable(drawable)

    }

}