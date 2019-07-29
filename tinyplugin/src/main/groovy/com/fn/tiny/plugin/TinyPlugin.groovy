package com.fn.tiny.plugin

import com.fn.tiny.TinyConstant
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * TinyPlugin
 * <p>
 * Author mmmy
 * Date 2019/7/23
 */
class TinyPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create(TinyConstant.GRADLE_CONFIG_NAME, TinyGradleConfig)
        project.afterEvaluate {
            project.task(TinyConstant.TASK_NAME, type: TinyTask)
        }
    }
}