class GitPlugin : Plugin<Project> {
    fun runGitCommand(project: Project, commands: List<String>): String {
        val gitDir = project.rootDir.resolve(".git")
        if (!gitDir.exists()) {
            throw GradleException("Not a git repository")
        }
        
        val process = ProcessBuilder(commands)
            .directory(project.rootDir)
            .redirectErrorStream(true)
            .start()
            
        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        
        if (exitCode != 0) {
            throw GradleException("Git command failed: $commands\n$output")
        }
        
        return output.trim()
    }

    override fun apply(project: Project) {
        project.task("describe") {
            doLast {
                val sourceFiles = project.fileTree("src").matching {
                    include("**/*.kt")
                    include("**/*.java")
                }.files

                val classFiles = project.fileTree("build").matching {
                    include("**/*.class") 
                }.files
                val gitOutput = runGitCommand(project, listOf("git", "log", "-1", "--pretty=format:\"%an | %s\""))
                println("Number of source files: ${sourceFiles.size}")
                println("Number of class files: ${classFiles.size}")
                println("Latest Git commit: ${gitOutput}")
            }
        }
    }
}

apply<GitPlugin>()
