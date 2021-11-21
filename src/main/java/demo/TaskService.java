package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class TaskService {
    @Autowired
    private TaskRepository repository;

    public List<TaskModel> getTasks(String title, String description, String assignedTo, TaskModel.TaskStatus status, TaskModel.TaskSeverity severity) {
        return repository.findAll().stream()
                .filter(task -> isMatch(task, title, description, assignedTo, status, severity))
                .collect(Collectors.toList());
    }
    private boolean isMatch (TaskModel task, String title, String description, String assignedTo, TaskModel.TaskStatus status, TaskModel.TaskSeverity severity) {
        return (title == null || task.getTitle().toLowerCase().startsWith(title.toLowerCase()))
                && (description == null || task.getDescription().toLowerCase().startsWith(description.toLowerCase()))
                && (assignedTo == null || task.getAssignedTo().toLowerCase().startsWith(assignedTo.toLowerCase()))
                && (status == null || task.getStatus().equals(status))
                && (severity == null || task.getSeverity().equals(severity));
    }

    public Optional<TaskModel> getTask(String id) {
        return repository.findById(id);
    }

    public TaskModel addTask (TaskModel task) throws IOException {
        repository.save(task);
        return task;
    }

    public boolean updateTask (String id, TaskModel task) throws IOException {
        if(repository.findById(id).isPresent()) {
            task.setId(id);
            repository.save(task);
            return true;
        } else {
            return false;
        }
    }

    public boolean patchTask (String id, TaskModel task) throws IOException {
        Optional<TaskModel> existingTask = repository.findById(id);
        if(existingTask.isPresent()) {
            existingTask.get().patch(task);
            repository.save(existingTask.get());
            return true;
        } else {
            return false;
        }
    }
    public boolean deleteTask (String id) throws IOException {
        return repository.deleteById(id);
    }

    public String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public String parseToCsv(Map<String, Object> map){
        List<String> output = new ArrayList<>();

        map.entrySet().forEach( x -> {
            StringBuilder builder = new StringBuilder();
            builder.append(x.getValue());
            output.add(builder.toString());
        });
        String response = output.toString();
        response = response.substring(1,response.length()-1);
        return response;
    }

    public String processCsv(List<Map<String, Object>> map){
        List<String> finalResponse = new ArrayList<>();
        StringBuilder builder1 = new StringBuilder();
        map.get(0).forEach( (key, value) -> {
            builder1.append(key + ",");
        });
        finalResponse.add(builder1.toString().substring(0, builder1.toString().length() - 1));

        map.forEach(x -> finalResponse.add(parseToCsv(x)));

        StringBuilder builder = new StringBuilder();
        builder.append(String.join("\n", finalResponse));
        return builder.toString();
    }

    public String parseToXml(Map<String, Object> map){
        List<String> output = new ArrayList<>();

        map.forEach( (key, value) -> {
            StringBuilder builder = new StringBuilder();
            builder.append("<" + key + ">" + value + "</" + key + ">\n");
            output.add(builder.toString());
        });
        String response = output.toString().replaceAll(",","");
        response = response.substring(1,response.length()-1);
        return response;
    }

    public String processXml(List<Map<String, Object>> map){
        List<String> finalResponse = new ArrayList<>();
        map.forEach(x -> finalResponse.add("\n<task>\n" + parseToXml(x) + "</task>") );
        StringBuilder builder = new StringBuilder();
        builder.append("<tasks>");
        builder.append(String.join("\n", finalResponse));
        builder.append("</tasks>");
        return builder.toString();
    }

}
