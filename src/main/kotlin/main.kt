import com.google.gson.Gson
import spark.Spark.exception
import spark.kotlin.Http
import spark.kotlin.RouteHandler
import spark.kotlin.halt
import spark.kotlin.ignite

val json = Gson()

data class Todo(val id: Int, val text: String)

private fun RouteHandler.id() = params("id").toInt()

private fun RouteHandler.parseTodo() = json.fromJson(request.body(), Todo::class.java)

fun main() {
    val http: Http = ignite()
    val todos: MutableList<Todo> = arrayListOf();

    exception(NumberFormatException::class.java) { _, _, _ -> halt(422, "id must be a number") }
    http.before { type("application/json") }

    http.get("/todos") { json.toJson(todos) }
    http.get("/todos/:id") {
        todos.find { e -> e.id == id() }?.let { e -> json.toJson(e) } ?: halt(404)
    }
    http.post("/todos/:id") {
        if (todos.any { e -> e.id == id() }) {
            halt(422, "Id ${id()} already exists, use PUT to edit")
        }

        val todo: Todo = parseTodo()
        todos.add(todo)
        json.toJson(todo)
    }
    http.put("/todos/:id") {
        val idx: Int = todos.indexOfFirst { e -> e.id == id() }
        if (idx == -1) {
            halt(404, "Not found")
        }
        val todo = parseTodo()
        todos[idx] = todo
        json.toJson(todo)
    }
    http.delete("/todos/:id") {
        val removed = todos.removeIf { e -> e.id == id() }
        if (removed) {
            halt(204, "Removed")
        } else {
            halt(404, "Not found")
        }
    }
}
