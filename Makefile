make:
	javac -d ./bin -sourcepath ./src src/valgrindpp/main/Main.java; \
	cp -r src/Definitions/ bin/Definitions; \
	cd bin/; \
	jar cvfe ../GradeScope/valgrindpp.jar valgrindpp.main.Main .
