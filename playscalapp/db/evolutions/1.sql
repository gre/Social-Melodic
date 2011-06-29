 
# --- !Ups
 
CREATE TABLE Family (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    name varchar(30) NOT NULL,
    status varchar(20) NOT NULL,
    root_id bigint(20),
    luckOfMutation double,
    luckOfJustIntonation double,
    luckToEscape1of2 double,
    luckToEscape1of4 double,
    luckToIgnoreSamePosition double,
    created date
);

CREATE TABLE Melody (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    parent_id bigint(20) NULL,
    family_id bigint(20) NOT NULL,
    loopLength int,
    notesLength int,
    likes int,
    total int,
    generation int,
    mutation int,
    sterile bool,
    created date,
    FOREIGN KEY (parent_id) REFERENCES Melody(id),
    FOREIGN KEY (family_id) REFERENCES Family(id)
);

ALTER TABLE Family ADD FOREIGN KEY (root_id) REFERENCES Melody(id);

CREATE TABLE Note (
    id bigint(20) NOT NULL AUTO_INCREMENT,
    pitch int NOT NULL,
    pos int NOT NULL,
    melody_id bigint(20) NOT NULL,
    FOREIGN KEY (melody_id) REFERENCES Melody(id)
);

 
# --- !Downs
 
DROP TABLE Family;
DROP TABLE Melody;
DROP TABLE Note;
