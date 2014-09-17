CREATE TABLE call_records 
(
    imsi VARCHAR(15),
    timestamp TIMESTAMP,
    duration INT,
    calling_number VARCHAR(15),
    called_number VARCHAR(15),
    primary key(imsi, timestamp)
);