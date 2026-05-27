package pl.studyshare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @GetMapping
    public String listTasks() {
        return "task-list";
    }

    @GetMapping("/{id}")
    public String taskDetail() {
        return "task-detail";
    }

    @GetMapping("/new")
    public String newTaskForm() {
        return "task-form";
    }
}