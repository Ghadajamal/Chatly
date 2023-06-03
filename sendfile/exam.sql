use parc_tableau;
CREATE TABLE Livre(
Num_livre INT PRIMARY KEY,
Titre VARCHAR(50) NOT NULL,
Auteur VARCHAR(50) NOT NULL,
Nbre_exmpl INT NOT NULL );
 
CREATE TABLE Abonne(
Num_abonne INT PRIMARY KEY,
Nom VARCHAR(50) NOT NULL,
Prenom VARCHAR(50) NOT NULL
 );
 
CREATE TABLE Pret(
Num_livre INT NOT NULL,
Num_abonne INT NOT NULL,
date_pret date NOT NULL,
FOREIGN KEY (Num_livre) REFERENCES Livre (Num_livre),
Constraint fk_1 FOREIGN KEY (Num_abonne) REFERENCES Abonne (Num_abonne),
Constraint pk_1 PRIMARY KEY (Num_livre,Num_abonne) 
 );
 
insert into Livre values(502, 'Bases De Données' ,'Auteur 1', 3);
insert into Livre values(533, 'Algorithmes' ,'Auteur 2', 5);
insert into Livre values(482, 'Langage python','Auteur 3 ',12);
insert into Livre values(578, 'Unix','Auteur 4 ',11);
insert into Livre values(112, 'Programmation' ,'Auteur 5', 5);
insert into Livre values(963, 'Langage C' ,'Auteur 6', 9);
insert into Livre values(531, 'Systèmes Exploitations' ,'Auteur 7', 8);
insert into Livre values(623, 'Langage Java','Auteur 8' ,4);
 
insert into Abonne values (100 ,'Ab11' ,'Ab12');
insert into Abonne values (102 ,'Ab21' ,'Ab22');
insert into Abonne values (55 ,'Ab31' ,'Ab32');
insert into Abonne values (45 ,'Ab41' ,'Ab42');
insert into Abonne values (125 ,'Ab51' ,'Ab52');
 

insert into pret values (533 ,100 ,05/15/2017);
insert into pret values (502, 45 ,06/13/2017);
insert into pret values (112 ,125 ,07/01/2017);
insert into pret values (533 ,102 ,10/01/2017);
insert into pret values (502, 55 ,10/02/2017);
insert into pret values (963 ,102 ,12/01/2017);
insert into pret values (502 ,125 ,01/03/2018);
insert into pret values (112 ,102 ,01/09/2018);
insert into pret values (502 ,102 ,01/12/2017);
