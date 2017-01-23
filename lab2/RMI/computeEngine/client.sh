java -cp /home/shan/learning/ds/DistributedSystem/lab2/RMI/computeEngine:/home/shan/public_html/classes/compute.jar
     -Djava.rmi.server.codebase=http://ubuntu/~shan/classes/
     -Djava.security.policy=client.policy
        client.ComputePi ubuntu.server 45
