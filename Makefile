make:
	javac -d ./bin -sourcepath ./src src/valgrindpp/main/Main.java; \
	cd bin/; \
	jar cvfe ../GradeScope/valgrindpp.jar valgrindpp.main.Main .
