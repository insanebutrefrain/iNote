package macom.inote.ui.pager

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import macom.inote.data.Todo
import macom.inote.ui.component.AlertEdit
import macom.inote.ui.component.DeleteConfirmAlert
import macom.inote.ui.pager.todoPager.TodoAddAlert
import macom.inote.ui.pager.todoPager.TodoCard
import macom.inote.ui.pager.todoPager.TodoTopBar
import macom.inote.viewModel.INoteIntent
import macom.inote.viewModel.INoteViewModel

val isEditTodo = mutableStateOf(false)
var selectedTodo: MutableState<Todo> =
    mutableStateOf(
        Todo(
            isOver = false,
            body = "",
            createTime = System.currentTimeMillis(),
            remindTime = null,
            user = "未知"
        )
    ) // todo user

/**
 * 待办页面设计
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "UnrememberedMutableState")
@Composable
fun TodoPager(
    buttonPadding: PaddingValues, navController: NavHostController, viewModel: INoteViewModel
) {
    val state = viewModel.state.collectAsState()
    val todos = state.value.todos
    val isExpanded = remember { mutableStateOf(false) }
    val isDeleteMode = remember { mutableStateOf(false) }
    val isShowDeleteAlert = remember { mutableStateOf(false) }
    val isDeleteMap = remember { mutableStateMapOf<Todo, Boolean>() }
    todos.forEach {
        isDeleteMap[it] = false
    }
    Scaffold(modifier = Modifier, topBar = {
        TodoTopBar(
            isDeleteMode = isDeleteMode,
            isDeleteMap = isDeleteMap,
            isDeleteAlert = isShowDeleteAlert,
            viewModel = viewModel,
            navController = navController
        )
    }) { topPadding ->
        //删除待办的弹窗
        DeleteConfirmAlert(isShowDeleteAlert = isShowDeleteAlert, onDismiss = {
            isShowDeleteAlert.value = false
        }, onDeleteConfirmed = {
            isDeleteMap.forEach {
                if (it.value) {
                    val intent = INoteIntent.DeleteTodo(it.key)
                    viewModel.deleteTodo(intent)
                    isDeleteMap[it.key] = false
                }
            }
            isDeleteMode.value = false
            isShowDeleteAlert.value = false
        })
        // 修改待办弹窗
        AlertEdit(title = "修改",
            isShow = isEditTodo,
            currentValue = selectedTodo.value.body,
            onDismiss = { isEditTodo.value = false },
            onConfirm = { newBody ->
                val intent = INoteIntent.EditTodo(newBody, selectedTodo!!.value)
                viewModel.editTodo(intent)
            })
        // 添加待办弹窗
        TodoAddAlert(viewModel = viewModel)
        LazyColumn(
            modifier = Modifier.padding(
                bottom = buttonPadding.calculateBottomPadding(),
                top = topPadding.calculateTopPadding()
            ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //还未完成的待办
            items(todos) { todo ->
                AnimatedVisibility(
                    visible = !todo.isOver, // 只在待办任务未完成时显示
                    enter = scaleIn(
                        initialScale = 0f, animationSpec = tween(
                            durationMillis = 500, easing = FastOutSlowInEasing
                        )
                    ), exit = fadeOut(
                        targetAlpha = 0f, animationSpec = tween(
                            durationMillis = 500, easing = FastOutSlowInEasing
                        )
                    )
                ) {
                    TodoCard(
                        todo = todo,
                        viewModel = viewModel,
                        isDeleteMode = isDeleteMode,
                        isDeleteMap = isDeleteMap
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(onClick = {
                        isExpanded.value = !isExpanded.value
                    }) {
                        Icon(
                            imageVector = if (isExpanded.value) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowRight,
                            contentDescription = null
                        )
                    }
                    Text("已完成")
                }
            }
            items(todos) { todo ->
                // 用 `AnimatedVisibility` 来实现动画
                AnimatedVisibility(
                    visible = isExpanded.value && todo.isOver, // 只在待办任务未完成时显示
                    enter = scaleIn(
                        initialScale = 0f, animationSpec = tween(
                            durationMillis = 500, easing = FastOutSlowInEasing
                        )
                    ), exit = fadeOut(
                        targetAlpha = 0f, animationSpec = tween(
                            durationMillis = 500, easing = FastOutSlowInEasing
                        )
                    )
                ) {
                    TodoCard(
                        todo = todo,
                        viewModel = viewModel,
                        isDeleteMode = isDeleteMode,
                        isDeleteMap = isDeleteMap
                    )
                }
            }
        }
    }
}
