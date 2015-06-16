# Import Timings

ImportBio4App GO_annotation (325188 lists) with 8GB heap (not required) : 08:08:01.329 .. 08:34:55.930 
From 36.3 MB Scheme to 258 MB Neo4j (~7x).

ImportBio4App GO_new (311234 lists) with 6GB heap (4GB heap throws GC error during commit) :  
From 38.3 MB Scheme to 289 MB Neo4j (~x).

    2015-06-16 12:02:01.832  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : Starting ImportBio4App on netadm.dev with PID 2368 (/home/ceefour/git/opencog-neo4j/target/classes started by ceefour in /home/ceefour/git/opencog-neo4j)
    2015-06-16 12:02:01.922  INFO 2368 --- [           main] s.c.a.AnnotationConfigApplicationContext : Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@3891771e: startup date [Tue Jun 16 12:02:01 WIB 2015]; root of context hierarchy
    2015-06-16 12:02:03.647  INFO 2368 --- [           main] f.a.AutowiredAnnotationBeanPostProcessor : JSR-330 'javax.inject.Inject' annotation found and supported for autowiring
    2015-06-16 12:02:03.774  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : Opening Neo4j database '/home/ceefour/tmp/go_new.neo4j'
    2015-06-16 12:02:06.007  INFO 2368 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
    2015-06-16 12:02:06.018  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : Ensuring constraints and indexes...
    2015-06-16 12:02:07.937  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : Ensured constraints and indexes.
    2015-06-16 12:02:07.938  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : Reading scheme file '/data/project_netadm/opencog/neo4j/Bio_schemeFiles/GO_new.scm'
    2015-06-16 12:02:14.023  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 0 of 311234 (0%)
    2015-06-16 12:02:24.411  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 1000 of 311234 (0%)
    2015-06-16 12:02:29.952  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 2000 of 311234 (0%)
    2015-06-16 12:02:35.155  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 3000 of 311234 (0%)
    2015-06-16 12:02:39.420  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 4000 of 311234 (1%)
    2015-06-16 12:02:45.945  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 5000 of 311234 (1%)
    2015-06-16 12:02:50.193  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 6000 of 311234 (1%)
    2015-06-16 12:02:54.954  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 7000 of 311234 (2%)
    2015-06-16 12:02:58.667  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 8000 of 311234 (2%)
    2015-06-16 12:03:02.374  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 9000 of 311234 (2%)
    2015-06-16 12:03:05.896  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 10000 of 311234 (3%)
    ...
    2015-06-16 12:32:45.940  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 301000 of 311234 (96%)
    2015-06-16 12:32:58.933  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 302000 of 311234 (97%)
    2015-06-16 12:33:12.114  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 303000 of 311234 (97%)
    2015-06-16 12:33:26.221  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 304000 of 311234 (97%)
    2015-06-16 12:33:41.295  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 305000 of 311234 (97%)
    2015-06-16 12:33:56.611  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 306000 of 311234 (98%)
    2015-06-16 12:34:10.787  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 307000 of 311234 (98%)
    2015-06-16 12:34:23.936  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 308000 of 311234 (98%)
    2015-06-16 12:34:36.698  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 309000 of 311234 (99%)
    2015-06-16 12:34:46.859  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 310000 of 311234 (99%)
    2015-06-16 12:34:58.685  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : List 311000 of 311234 (99%)
    2015-06-16 12:34:59.540  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : Committing transaction...
    2015-06-16 12:35:41.044  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : Done
    2015-06-16 12:35:41.052  INFO 2368 --- [           main] org.opencog.neo4j.ImportBio4App          : Started ImportBio4App in 2019.688 seconds (JVM running for 2020.385)
    2015-06-16 12:35:41.058  INFO 2368 --- [       Thread-1] s.c.a.AnnotationConfigApplicationContext : Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@3891771e: startup date [Tue Jun 16 12:02:01 WIB 2015]; root of context hierarchy
    2015-06-16 12:35:41.066  INFO 2368 --- [       Thread-1] o.s.j.e.a.AnnotationMBeanExporter        : Unregistering JMX-exposed beans on shutdown
