package com.github.johnnyhooyo.dsaiassist.startup;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MyProjectActivity implements ProjectActivity {
    
    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        // 项目启动时的初始化逻辑
        return Unit.INSTANCE;
    }
}
