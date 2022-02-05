/*
 * QAuxiliary - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2022 qwq233@qwq2333.top
 * https://github.com/cinit/QAuxiliary
 *
 * This software is non-free but opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version and our eula as published
 * by QAuxiliary contributors.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 * <https://github.com/cinit/QAuxiliary/blob/master/LICENSE.md>.
 */

package io.github.qauxv.hook

import android.app.Activity
import android.content.Context
import android.view.View
import io.github.qauxv.SyncUtils
import io.github.qauxv.base.ISwitchCellAgent
import io.github.qauxv.base.IUiItemAgent
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A function that only has a enable/disable switch function.
 */
abstract class CommonSwitchFunctionHook(
    hookKey: String? = null,
    defaultEnabled: Boolean = false,
    dexDeobfIndexes: IntArray? = null,
    private val targetProc: Int = SyncUtils.PROC_MAIN
) : BaseFunctionHook(hookKey, defaultEnabled, dexDeobfIndexes = dexDeobfIndexes) {

    constructor() : this(null, false)
    constructor(defaultEnabled: Boolean) : this(null, defaultEnabled)
    constructor(key: String) : this(key, false)
    constructor(key: String, dexDeobfIndexes: IntArray) : this(key, false, dexDeobfIndexes)
    constructor(dexDeobfIndexes: IntArray) : this(null, false, dexDeobfIndexes)
    constructor(key: String, targetProc: Int) : this(hookKey = key, targetProc = targetProc)
    constructor(targetProc: Int) : this(null, targetProc = targetProc)
    constructor(targetProc: Int, dexDeobfIndexes: IntArray) : this(null, targetProc = targetProc, dexDeobfIndexes = dexDeobfIndexes)

    /**
     * Name of the function.
     */
    abstract val name: String

    /**
     * Description of the function.
     */
    open val description: String? = null

    override val targetProcesses = targetProc

    open val extraSearchKeywords: List<String>? = null

    override val uiItemAgent: IUiItemAgent by lazy {
        object : IUiItemAgent {
            override val titleProvider: (IUiItemAgent) -> String = { _ -> name }
            override val summaryProvider: (IUiItemAgent, Context) -> String? = { _, _ -> description }
            override val valueState: MutableStateFlow<String?>? = null
            override val validator: ((IUiItemAgent) -> Boolean) = { _ -> true }
            override val switchProvider: ISwitchCellAgent? by lazy {
                object : ISwitchCellAgent {
                    override val isCheckable = true
                    override var isChecked: Boolean
                        get() = isEnabled
                        set(value) {
                            if (value != isEnabled) {
                                isEnabled = value
                            }
                        }
                }
            }
            override val onClickListener: ((IUiItemAgent, Activity, View) -> Unit)? = null
            override val extraSearchKeywordProvider: ((IUiItemAgent, Context) -> List<String>?)?
                get() = extraSearchKeywords?.let { { _, _ -> it } }
        }
    }
}