# Import Timings

## ImportBio5App (public from test-datasets)

ImportBio5App GO (324240 lists) with 4GB heap : 1663 seconds
From 36.3 MB Scheme to 200 MB Neo4j.

    2015-06-19 21:21:53.561  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : Starting ImportBio5App on netadm.dev with PID 23549 (/home/ceefour/git/opencog-neo4j/target/classes started by ceefour in /home/ceefour/git/opencog-neo4j)
    2015-06-19 21:21:53.703  INFO 23549 --- [           main] s.c.a.AnnotationConfigApplicationContext : Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@2de8284b: startup date [Fri Jun 19 21:21:53 WIB 2015]; root of context hierarchy
    2015-06-19 21:21:55.690  INFO 23549 --- [           main] f.a.AutowiredAnnotationBeanPostProcessor : JSR-330 'javax.inject.Inject' annotation found and supported for autowiring
    2015-06-19 21:21:55.848  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : Opening Neo4j database '/home/ceefour/tmp/go.neo4j'
    2015-06-19 21:21:59.086  INFO 23549 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
    2015-06-19 21:21:59.102  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : Ensuring constraints and indexes...
    2015-06-19 21:22:01.207  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : Ensured constraints and indexes.
    2015-06-19 21:22:01.208  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : Reading scheme file '/data/project_netadm/opencog/agi-bio/GO.scm'
    2015-06-19 21:22:10.368  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : List 0 of 324240 (0%)
    2015-06-19 21:22:25.819  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : List 1000 of 324240 (0%)
    2015-06-19 21:22:33.297  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : List 2000 of 324240 (0%)
    2015-06-19 21:22:39.629  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : List 3000 of 324240 (0%)
    2015-06-19 21:22:44.382  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : List 4000 of 324240 (1%)
    2015-06-19 21:22:50.637  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : List 5000 of 324240 (1%)
    ...
    2015-06-19 21:48:41.550  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : List 319000 of 324240 (98%)
    2015-06-19 21:48:46.388  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : List 320000 of 324240 (98%)
    2015-06-19 21:48:49.974  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : List 321000 of 324240 (99%)
    2015-06-19 21:48:53.583  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : List 322000 of 324240 (99%)
    2015-06-19 21:48:57.260  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : List 323000 of 324240 (99%)
    2015-06-19 21:49:00.682  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : List 324000 of 324240 (99%)
    2015-06-19 21:49:01.761  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : Committing transaction...
    2015-06-19 21:49:35.162  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : Done
    2015-06-19 21:49:35.248  INFO 23549 --- [           main] org.opencog.neo4j.ImportBio5App          : Started ImportBio5App in 1662.18 seconds (JVM running for 1663.611)
    2015-06-19 21:49:35.254  INFO 23549 --- [       Thread-1] s.c.a.AnnotationConfigApplicationContext : Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@2de8284b: startup date [Fri Jun 19 21:21:53 WIB 2015]; root of context hierarchy
    2015-06-19 21:49:35.266  INFO 23549 --- [       Thread-1] o.s.j.e.a.AnnotationMBeanExporter        : Unregistering JMX-exposed beans on shutdown

ImportBio5App GO_annotation (324240 lists) with 6GB heap : 821 seconds
From 39.9 MB Scheme to 89.2 MB Neo4j.

    2015-06-19 20:58:09.139  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : Starting ImportBio5App on netadm.dev with PID 21565 (/home/ceefour/git/opencog-neo4j/target/classes started by ceefour in /home/ceefour/git/opencog-neo4j)
    2015-06-19 20:58:09.261  INFO 21565 --- [           main] s.c.a.AnnotationConfigApplicationContext : Refreshing org.springframework.context.annotation.AnnotationConfigApplicationContext@2de8284b: startup date [Fri Jun 19 20:58:09 WIB 2015]; root of context hierarchy
    2015-06-19 20:58:11.411  INFO 21565 --- [           main] f.a.AutowiredAnnotationBeanPostProcessor : JSR-330 'javax.inject.Inject' annotation found and supported for autowiring
    2015-06-19 20:58:11.682  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : Opening Neo4j database '/home/ceefour/tmp/go_annotation.neo4j'
    2015-06-19 20:58:15.256  INFO 21565 --- [           main] o.s.j.e.a.AnnotationMBeanExporter        : Registering beans for JMX exposure on startup
    2015-06-19 20:58:15.277  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : Ensuring constraints and indexes...
    2015-06-19 20:58:17.229  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : Ensured constraints and indexes.
    2015-06-19 20:58:17.230  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : Reading scheme file '/data/project_netadm/opencog/agi-bio/GO_annotation.scm'
    2015-06-19 20:58:24.208  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : List 0 of 369755 (0%)
    2015-06-19 20:58:32.333  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : List 1000 of 369755 (0%)
    2015-06-19 20:58:36.859  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : List 2000 of 369755 (0%)
    2015-06-19 20:58:40.962  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : List 3000 of 369755 (0%)
    2015-06-19 20:58:45.064  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : List 4000 of 369755 (1%)
    2015-06-19 20:58:47.783  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : List 5000 of 369755 (1%)
    ...
    2015-06-19 21:10:58.438  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : List 364000 of 369755 (98%)
    2015-06-19 21:11:00.490  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : List 365000 of 369755 (98%)
    2015-06-19 21:11:02.516  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : List 366000 of 369755 (98%)
    2015-06-19 21:11:04.923  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : List 367000 of 369755 (99%)
    2015-06-19 21:11:07.437  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : List 368000 of 369755 (99%)
    2015-06-19 21:11:15.628  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : List 369000 of 369755 (99%)
    2015-06-19 21:11:17.281  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : Committing transaction...
    2015-06-19 21:11:50.393  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : Done
    2015-06-19 21:11:50.397  INFO 21565 --- [           main] org.opencog.neo4j.ImportBio5App          : Started ImportBio5App in 821.823 seconds (JVM running for 822.596)
    2015-06-19 21:11:50.404  INFO 21565 --- [       Thread-1] s.c.a.AnnotationConfigApplicationContext : Closing org.springframework.context.annotation.AnnotationConfigApplicationContext@2de8284b: startup date [Fri Jun 19 20:58:09 WIB 2015]; root of context hierarchy
    2015-06-19 21:11:50.407  INFO 21565 --- [       Thread-1] o.s.j.e.a.AnnotationMBeanExporter        : Unregistering JMX-exposed beans on shutdown

## ImportBio5App (private bio scheme files)

ImportBio5App GO_annotation (325188 lists) with 4GB heap : 12:57:57..13:20:53
From 36.3 MB Scheme to 253 MB Neo4j (~7×).

ImportBio5App GO_new (311234 lists) with 6GB heap : 14:13:51..14:45:47
From 38.3 MB Scheme to 229 MB Neo4j (~7×).

## ImportBio4App

ImportBio4App GO_annotation (325188 lists) with 8GB heap (not required) : 08:08:01.329 .. 08:34:55.930 
From 36.3 MB Scheme to 258 MB Neo4j (~7×).

ImportBio4App GO_new (311234 lists) with 6GB heap (4GB heap throws GC error during commit) :  
From 38.3 MB Scheme to 289 MB Neo4j (~×).

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
