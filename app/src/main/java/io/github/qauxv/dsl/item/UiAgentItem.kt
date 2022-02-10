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

package io.github.qauxv.dsl.item

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import io.github.qauxv.base.IDynamicHook
import io.github.qauxv.base.ISwitchCellAgent
import io.github.qauxv.base.IUiItemAgentProvider
import io.github.qauxv.core.HookInstaller
import io.github.qauxv.dsl.cell.TitleValueCell
import io.github.qauxv.dsl.func.IDslItemNode
import io.github.qauxv.util.Toasts
import io.github.qauxv.util.hostInfo

class UiAgentItem(
        override val identifier: String,
        override val name: String,
        val agentProvider: IUiItemAgentProvider,
) : IDslItemNode, TMsgListItem {

    override val isSearchable: Boolean = true
    override val isClickable: Boolean get() = isEnabled
    override val isEnabled: Boolean
        get() {
            val agent = agentProvider.uiItemAgent
            return agent.validator?.invoke(agent) ?: true
        }

    override val isVoidBackground: Boolean = false

    class HeaderViewHolder(cell: TitleValueCell) : RecyclerView.ViewHolder(cell)

    override fun createViewHolder(context: Context, parent: ViewGroup): RecyclerView.ViewHolder {
        return HeaderViewHolder(TitleValueCell(context))
    }

    private val mCheckChangedListener = CompoundButton.OnCheckedChangeListener { btn, isChecked ->
        val agent = agentProvider.uiItemAgent
        val switchCellAgent = agent.switchProvider
        switchCellAgent?.isChecked = isChecked
        // if the function is enabled but not initialized, initialize it
        if (agentProvider is IDynamicHook) {
            val hook: IDynamicHook = agentProvider
            val context = btn.context
            if (hook.isEnabled && !hook.isInitialized) {
                // we need to initialize the hook
                HookInstaller.initializeHookForeground(context, hook)
            }
            if (hook.isApplicationRestartRequired) {
                Toasts.info(context, "重启 ${hostInfo.hostName} 生效")
            }
        }
    }

    private val mOnClickListener = View.OnClickListener {
        onItemClick(it, -1, -1, -1)
    }

    override fun bindView(viewHolder: RecyclerView.ViewHolder, position: Int, context: Context) {
        val cell = viewHolder.itemView as TitleValueCell
        val agent = agentProvider.uiItemAgent
        cell.title = agent.titleProvider.invoke(agent)
        val description: String? = agent.summaryProvider?.invoke(agent, context)
        val valueState = agent.valueState
        // value state observers are registered in the fragment, we only need to update the value
        val valueStateValue: String? = valueState?.value
        val switchAgent: ISwitchCellAgent? = agent.switchProvider
        if (switchAgent != null) {
            // has switch!!, must not both have a switch and a value
            var toBeShownAtSummary: String? = valueState?.value
            if (valueStateValue.isNullOrEmpty()) {
                toBeShownAtSummary = description
            }
            cell.summary = if (toBeShownAtSummary.isNullOrEmpty()) null else toBeShownAtSummary
            cell.isHasSwitch = true
            cell.isChecked = switchAgent.isChecked
            cell.switchView.isEnabled = switchAgent.isCheckable
            cell.switchView.isClickable = switchAgent.isCheckable
            cell.switchView.setOnCheckedChangeListener(mCheckChangedListener)
        } else {
            // simple case, as it is
            cell.summary = description
            cell.value = valueStateValue
        }
        cell.setOnClickListener(mOnClickListener)
    }

    override fun onItemClick(v: View, position: Int, x: Int, y: Int) {
        val agent = agentProvider.uiItemAgent
        val cell = v as TitleValueCell
        // TODO: 2022-02-09 if ClassCastException, it means the context is not Activity use base.context
        val activity: Activity = v.context as Activity
        val onClick = agent.onClickListener
        if (onClick != null) {
            onClick.invoke(agent, activity, v)
        } else {
            // check if it has switch
            if (cell.isHasSwitch) {
                cell.switchView.toggle()
            }
        }
    }

    override val isLongClickable: Boolean = false

    override fun onLongClick(v: View, position: Int, x: Int, y: Int): Boolean {
        // nop
        return false
    }
}
