#/bin/bash
#Automate the swapping process for running docker images
#@param img: name of the image to swap to

function killitif {
    docker ps -a  > /tmp/yy_xx$$
    if grep --quiet $1 /tmp/yy_xx$$
     then
     echo "killing older version of $1"
     docker rm -f `docker ps -a | grep $1  | sed -e 's: .*$::'`
   fi
}

case $# in
[!1]) echo 'Usage: ' $0 '<name of Docker image>'; exit 1;;
esac

#First check to see if the image is running process
#But since I don't know how to do that...

#First check to see which image is trying to be swapped: web1 or web2
#Check to see that the argument is valid
if [ $1 != "web1" -a $1 != "web2" ]
    echo "$0: first argument must be a valid 'web1' or 'web2'"
    exit 1
fi

if [ $1 == "web1" ]
    killitif web2
    killitif proxy

    docker rm -f ecs189_web2

    #Verify network configuration
    docker network ls
    ecs189_default network

    docker run --name ecs189_web1_1 --network ecs189_default -d -P activity
    docker exec ecs189_proxy_1 /bin/bash bin/swap1.sh

    #Verify that swapping was successful
    docker ps -a

fi

if [ $1 == "web2" ]
    killitif web1
    killitif proxy

    docker rm -f ecs189_web1_1

    #Verify network configuration
    docker network ls
    ecs189_default network

    docker run --name ecs189_web2 --network ecs189_default -d -P activity2
    docker exec ecs189_proxy_1 /bin/bash bin/swap2.sh

    #Verify that swapping was successful
    docker ps -a
fi