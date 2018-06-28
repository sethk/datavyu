curl http://www.datavyu.org/docs/user-guide.pdf > packaged_docs/user-guide.pdf
# Older version until 1.4.0
# mvn -Prelease,jar-package -Dmaven.test.skip=true clean package jar:jar assembly:assembly
# Version 1.4.1
mvn -Prelease,jar-package -Djavacpp.platform=linux-x86_64 -Dmaven.test.skip=true clean package jar:jar assembly:assembly
