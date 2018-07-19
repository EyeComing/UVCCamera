package com.serenegiant.widget;
/*
 * libcommon
 * utility/helper classes for myself
 *
 * Copyright (c) 2014-2018 saki t_saki@serenegiant.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
*/

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import com.serenegiant.common.R;

public class TimePickerPreference extends DialogPreference {

	private final Calendar calendar;
	private final long mDefaultValue;
	private TimePicker picker = null;

	public TimePickerPreference(final Context ctxt) {
		this(ctxt, null);
	}

	public TimePickerPreference(final Context ctxt, final AttributeSet attrs) {
		this(ctxt, attrs, android.R.attr.dialogPreferenceStyle);
	}

	public TimePickerPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(
        		attrs, R.styleable.TimePicker, defStyle, 0);
        mDefaultValue = (long)a.getFloat(R.styleable.TimePicker_TimePickerDefaultValue, -1);
        a.recycle();
        a = null;

        setPositiveButtonText(android.R.string.ok);
		setNegativeButtonText(android.R.string.cancel);
		calendar = new GregorianCalendar();
	}

	@Override
	protected View onCreateDialogView() {
		picker = new TimePicker(getContext());
		picker.setIs24HourView(true);
		return (picker);
	}

	@Override
	protected void onBindDialogView(final View v) {
		super.onBindDialogView(v);
		picker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		picker.setCurrentMinute(calendar.get(Calendar.MINUTE));
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		super.onDialogClosed(positiveResult);

		if (positiveResult) {
			calendar.set(Calendar.HOUR_OF_DAY, picker.getCurrentHour());
			calendar.set(Calendar.MINUTE, picker.getCurrentMinute());

			setSummary(getSummary());
			if (callChangeListener(calendar.getTimeInMillis())) {
				persistLong(calendar.getTimeInMillis());
				notifyChanged();
			}
		}
	}

	@Override
	protected Object onGetDefaultValue(final TypedArray a, final int index) {
		return (a.getString(index));
	}

	@Override
	protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {
		final long v = mDefaultValue > 0 ? mDefaultValue : System.currentTimeMillis();
		if (restoreValue) {
			long persistedValue;
			try {
				persistedValue = getPersistedLong(v);
			} catch (final Exception e) {
				// Stale persisted data may be the wrong type
				persistedValue = v;
			}
			calendar.setTimeInMillis(persistedValue);
		} else if (defaultValue != null) {
			calendar.setTimeInMillis(Long.parseLong((String) defaultValue));
		} else {
			// !restoreValue, defaultValue == null
			calendar.setTimeInMillis(v);
		}

		setSummary(getSummary());
	}

	@Override
	public CharSequence getSummary() {
		if (calendar == null) {
			return null;
		}
		return DateFormat.getTimeFormat(getContext()).format(
				new Date(calendar.getTimeInMillis()));
	}
}
