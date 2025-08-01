package macom.inote.ui.pager.taskPager

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import macom.inote.data.Task
import macom.inote.data.TaskList
import macom.inote.ui.component.TaskAndTodoAlertDialog
import macom.inote.ui.pager.todoPager.RepeatType
import macom.inote.viewModel.INoteIntent
import macom.inote.viewModel.INoteViewModel
import java.util.Calendar

val taskIsAdd = mutableStateOf(value = false)

@Composable
fun TaskAddAlert(viewModel: INoteViewModel, nowTaskList: MutableState<TaskList>) {
    TaskAndTodoAlertDialog(
        viewModel = viewModel,
        nowTaskList = nowTaskList,
        isShowDialog = taskIsAdd,
        onConfirm = { task, todo, viewModel, nowTaskList, isShowDialog, text, remindDateTime, isSetReminder, repeatType, context: Context ->
            addTaskOnConfirm(
                text = text,
                remindDateTime = remindDateTime,
                nowTaskList = nowTaskList,
                viewModel = viewModel,
                context = context,
                isShowDialog = isShowDialog,
                isSetReminder = isSetReminder,
                repeatType = repeatType,
            )
        },
    )
}

private fun addTaskOnConfirm(
    text: String,
    remindDateTime: Calendar,
    isSetReminder: Boolean,
    nowTaskList: MutableState<TaskList>?,
    viewModel: INoteViewModel,
    context: Context,
    isShowDialog: MutableState<Boolean>,
    repeatType: RepeatType
) {
    if (text.isNotEmpty()) {
        val newTask = Task(
            isOver = false,
            body = text,
            createTime = System.currentTimeMillis(),
            remindTime = if (isSetReminder) remindDateTime.timeInMillis else null,
            taskListId = nowTaskList!!.value.createTime,
            user = viewModel.getUser()!!,
            repeatPeriod = repeatType.value,
        )
        val intent = INoteIntent.AddTask(
            newTask = newTask, nowTaskList = nowTaskList.value
        )
        viewModel.addTask(intent)
        if (isSetReminder) viewModel.scheduleNoteReminder(
            id = newTask.user + newTask.createTime,
            title = "iNote 任务",
            content = newTask.body,
            delayInMillis = remindDateTime.timeInMillis - System.currentTimeMillis(),
            repeatPeriod = repeatType.value
        )
        Toast.makeText(
            context, "新建任务成功", Toast.LENGTH_SHORT
        ).show()
        isShowDialog.value = false  // 关闭Dialog
    } else {
        Toast.makeText(
            context, "任务内容为空！", Toast.LENGTH_SHORT
        ).show()
    }
}
