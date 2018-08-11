``S3 Source Producer

Use this with the following infrastructure command
''
sudo docker run --rm --net=host landoop/fast-data-dev

docker run --rm -t -p 2181:2181 -p 3030:3030 -p 8080:8080 -p 8082:8082 -p 8083:8083 -p 9092:9092 -e ADV_HOST=127.0.0.1 landoop/fast-data-dev


sudo docker run --name nifi -p 8080:8080 -d apache/nifi:latest
