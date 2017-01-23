java -cp /home/xz/learning/ds/DistributedSystem/lab2/RMI/computeEngine:/home/xz/public_html/classes/compute.jar
     -Djava.rmi.server.codebase=http://ubuntu/~xz/classes/compute.jar
     -Djava.rmi.server.hostname=ubuntu.server
     -Djava.security.policy=server.policy
        engine.ComputeEngine
