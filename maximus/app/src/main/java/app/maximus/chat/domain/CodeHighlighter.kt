package app.maximus.chat.domain

/**
 * Single-pass lexer for syntax colouring of code blocks (Kotlin, Java, Python, C/C++, JS/TS, Rust, Go,
 * shell, SQL). It recognises comments, strings, numbers, keywords and capitalised type names; anything
 * else stays plain. Linear time, so it also runs on a code block that is still streaming in.
 */
object CodeHighlighter {
    enum class Kind { KEYWORD, STRING, COMMENT, NUMBER, TYPE }

    data class Token(val start: Int, val end: Int, val kind: Kind)

    private val KEYWORDS = setOf(
        // Kotlin / Java
        "fun", "val", "var", "class", "object", "interface", "data", "sealed", "enum", "when", "if", "else", "for", "while", "do",
        "return", "break", "continue", "in", "is", "as", "null", "true", "false", "this", "super", "private", "public", "protected",
        "internal", "override", "open", "abstract", "final", "static", "void", "new", "import", "package", "try", "catch", "finally",
        "throw", "throws", "suspend", "inline", "companion", "const", "lateinit", "by", "lazy", "typealias", "extends", "implements",
        "int", "long", "double", "float", "boolean", "char", "byte", "short",
        // Python
        "def", "lambda", "pass", "yield", "with", "from", "None", "True", "False", "and", "or", "not", "elif", "except", "raise",
        "global", "nonlocal", "async", "await", "self",
        // C / C++ / Rust / Go / JS
        "struct", "union", "typedef", "template", "typename", "namespace", "using", "auto", "unsigned", "signed", "sizeof", "include",
        "define", "let", "mut", "impl", "trait", "pub", "match", "loop", "crate", "use", "mod", "func", "go", "defer", "chan", "map",
        "range", "function", "const", "export", "default", "switch", "case", "undefined", "typeof", "instanceof",
        // shell / SQL (upper case is matched case-insensitively for SQL)
        "echo", "then", "fi", "done", "esac", "select", "where", "insert", "update", "delete", "create", "table", "join", "group",
        "order", "values", "into", "set", "limit"
    )

    private fun hashComments(language: String) = language.lowercase() in setOf("python", "py", "bash", "sh", "shell", "zsh", "r", "yaml", "toml")

    fun tokens(code: String, language: String = ""): List<Token> {
        val out = ArrayList<Token>()
        val hash = hashComments(language)
        val sql = language.equals("sql", true)
        var i = 0
        val n = code.length
        while (i < n) {
            val c = code[i]
            when {
                c == '/' && i + 1 < n && code[i + 1] == '/' || (hash && c == '#') || (sql && c == '-' && i + 1 < n && code[i + 1] == '-') -> {
                    val end = code.indexOf('\n', i).let { if (it < 0) n else it }
                    out += Token(i, end, Kind.COMMENT); i = end
                }
                c == '/' && i + 1 < n && code[i + 1] == '*' -> {
                    val end = code.indexOf("*/", i + 2).let { if (it < 0) n else it + 2 }
                    out += Token(i, end, Kind.COMMENT); i = end
                }
                c == '"' || c == '\'' || c == '`' -> {
                    val triple = i + 2 < n && code[i + 1] == c && code[i + 2] == c
                    var j = i + if (triple) 3 else 1
                    while (j < n) {
                        if (code[j] == '\\') { j += 2; continue }
                        if (triple) { if (code.startsWith("$c$c$c", j)) { j += 3; break } }
                        else if (code[j] == c) { j++; break } else if (code[j] == '\n' && c != '`') break
                        j++
                    }
                    j = minOf(j, n)
                    out += Token(i, j, Kind.STRING); i = j
                }
                c.isDigit() && (i == 0 || !code[i - 1].isLetterOrDigit() && code[i - 1] != '_') -> {
                    var j = i + 1
                    while (j < n && (code[j].isLetterOrDigit() || code[j] == '.' || code[j] == '_') && !(code[j] == '.' && j + 1 < n && !code[j + 1].isDigit())) j++
                    out += Token(i, j, Kind.NUMBER); i = j
                }
                c.isLetter() || c == '_' -> {
                    var j = i + 1
                    while (j < n && (code[j].isLetterOrDigit() || code[j] == '_')) j++
                    val word = code.substring(i, j)
                    when {
                        word in KEYWORDS || (sql && word.lowercase() in KEYWORDS) -> out += Token(i, j, Kind.KEYWORD)
                        word[0].isUpperCase() && word.length > 1 && word.any { it.isLowerCase() } -> out += Token(i, j, Kind.TYPE)
                    }
                    i = j
                }
                else -> i++
            }
        }
        return out
    }
}
