SET REFERENTIAL_INTEGRITY FALSE;

-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `profissional`
--

DROP TABLE IF EXISTS `profissional`;
CREATE TABLE `profissional` (
  `id_profissional` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(150) NOT NULL,
  `cpf` varchar(14) NOT NULL,
  `data_nascimento` date NOT NULL,
  `genero` varchar(30) DEFAULT NULL,
  `telefone` varchar(20) DEFAULT NULL,
  `email` varchar(150) DEFAULT NULL,
  `numero_conselho` varchar(30) DEFAULT NULL,
  `tipo_conselho` varchar(20) DEFAULT NULL,
  `uf_conselho` char(2) DEFAULT NULL,
  `funcao` varchar(100) NOT NULL,
  `tipo_vinculo` varchar(50) NOT NULL,
  `percentual_comissao` decimal(5,2) DEFAULT NULL,
  `dias_horario_trabalho` varchar(255) DEFAULT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'Ativo',
  `data_admissao` date DEFAULT NULL,
  `data_desligamento` date DEFAULT NULL,
  PRIMARY KEY (`id_profissional`),
  UNIQUE KEY `uq_profissional_cpf` (`cpf`)
);

--
-- Dumping data for table `profissional`
--

INSERT INTO `profissional` VALUES (1,'Camila Rocha','111.222.333-44','1982-05-10','F','(11) 91111-0001','camila.rocha@bellapelle.com','CRBM 55210',NULL,NULL,'Biomédica esteta - Responsável técnica','Sócio',NULL,'Seg a Sex, 9h-18h','Ativo','2022-01-10',NULL),(2,'Carla Menezes','222.333.444-55','1987-02-18',NULL,'(11) 91111-0002','carla.menezes@bellapelle.com','COREN 123456',NULL,NULL,'Biomédica esteta','Prestador de serviço',30.00,'Seg a Sáb, 9h-19h','Ativo','2023-03-01',NULL),(3,'Rafael Torres','333.444.555-66','1990-08-25','M','(11) 91111-0003','rafael.torres@bellapelle.com','CRF 98765',NULL,NULL,'Farmacêutico esteta','Prestador de serviço',25.00,'Ter a Sáb, 10h-19h','Ativo','2023-06-15',NULL),(4,'Juliana Prado','444.555.666-77','1995-12-05','F','(11) 91111-0004','juliana.prado@bellapelle.com','Registro em andamento',NULL,NULL,'Esteticista','CLT',20.00,'Seg a Sex, 9h-18h','Ativo','2024-01-20',NULL),(5,'Brenda Godoy','388.967.413-27','1996-05-22','F','11947316180','brendagodoy@email.com','224701','CRBM','SP','Biomedica Esteta','CLT',20.00,'seg a sab 9:00 as 17:00','Ativo','2026-10-14',NULL);


-- Dump completed on 2026-10-25 15:08:33


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `sala`
--

DROP TABLE IF EXISTS `sala`;
CREATE TABLE `sala` (
  CREATE TABLE `sala` (
  `id_sala` int NOT NULL AUTO_INCREMENT,
  `numero` int NOT NULL,
  `nome` varchar(50) NOT NULL,
  `descricao` varchar(255) DEFAULT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'Ativa',
  PRIMARY KEY (`id_sala`),
  CONSTRAINT `uk_sala_numero` UNIQUE (`numero`)
);

INSERT INTO `sala` (`id_sala`,`numero`,`nome`,`descricao`,`status`)
 VALUES (1,1,'Sala 1','Sala liberada para todos os procedimentos','Ativa'),
 (2,2,'Sala 2','Sala liberada para todos os procedimentos','Ativa'),
 (3,3,'Sala 3','Sala liberada para todos os procedimentos','Ativa'),
 (4,4,'Sala 4','em construção','Em manutenção');



-- Dump completed on 2026-10-25 15:08:32


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `equipamento`
--

DROP TABLE IF EXISTS `equipamento`;
CREATE TABLE `equipamento` (
  `id_equipamento` int NOT NULL AUTO_INCREMENT,
  `nome_modelo` varchar(150) NOT NULL,
  `tipo` varchar(100) NOT NULL,
  `numero_serie` varchar(100) DEFAULT NULL,
  `id_sala` int DEFAULT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'Disponível',
  `ultima_manutencao` date DEFAULT NULL,
  `proxima_manutencao` date DEFAULT NULL,
  PRIMARY KEY (`id_equipamento`),
  CONSTRAINT `fk_equipamento_sala` FOREIGN KEY (`id_sala`) REFERENCES `sala` (`id_sala`) ON DELETE RESTRICT ON UPDATE CASCADE
);

--
-- Dumping data for table `equipamento`
--

INSERT INTO `equipamento` VALUES (1,'Criolipólise 1','Criolipólise','NS-CR-001',NULL,'Disponível','2026-06-12','2026-12-12'),(2,'Laser 2','Depilação a laser','NS-LZ-002',NULL,'Disponível','2026-08-28','2026-09-02'),(3,'Ozônio 1','Ozonioterapia','NS-OZ-001',NULL,'Disponível','2026-07-03','2027-01-03'),(4,'RF 1','Radiofrequência','NS-RF-001',NULL,'Disponível','2026-07-15','2027-01-15'),(5,'Carboxiterapia 1','Carboxiterapia','NS-CX-001',NULL,'Disponível','2026-07-20','2027-01-20'),(6,'Depilação a laser','Depilação a laser','2147390111',NULL,'Disponível','2026-05-30','2026-11-30'),(7,'ppppppp','aerserwer',NULL,NULL,'Inativo',NULL,NULL),(8,'RF 2','Radiofrequencia 2','NSRF002',NULL,'Disponível','2026-10-20','2026-04-20');


-- Dump completed on 2026-10-25 15:08:33


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `procedimento`
--

DROP TABLE IF EXISTS `procedimento`;
CREATE TABLE `procedimento` (
  `id_procedimento` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(150) NOT NULL,
  `descricao` text,
  `categoria` varchar(100) DEFAULT NULL,
  `valor` decimal(10,2) NOT NULL,
  `percentual_comissao` decimal(5,2) DEFAULT NULL,
  `duracao_minutos` int NOT NULL,
  `requer_equipamento` tinyint(1) NOT NULL DEFAULT '0',
  `tipo_equipamento_requerido` varchar(100) DEFAULT NULL,
  `contraindicacoes` text,
  `is_injetavel` tinyint(1) NOT NULL DEFAULT '0',
  `status` varchar(50) NOT NULL DEFAULT 'Ativo',
  PRIMARY KEY (`id_procedimento`)
);

--
-- Dumping data for table `procedimento`
--

INSERT INTO `procedimento` VALUES (1,'Criolipólise','Redução de gordura localizada por congelamento controlado','Corporal',240.00,NULL,45,1,'Criolipólise','Gestantes; problemas circulatórios graves',0,'Ativo'),(2,'Depilação a laser','Remoção de pelos por fotodepilação','Corporal',195.00,NULL,30,1,'Depilação a laser','Uso de isotretinoína; gestantes; pele bronzeada recentemente',0,'Ativo'),(3,'Radiofrequência','Estímulo de colágeno e firmeza da pele','Facial/Corporal',210.00,NULL,45,1,'Radiofrequência','Uso de marca-passo; gestantes',0,'Ativo'),(4,'Carboxiterapia','Aplicação subcutânea de CO2 para gordura localizada e flacidez','Corporal',180.00,39.00,40,1,'Carboxiterapia','Gestantes; doenças pulmonares graves',0,'Ativo'),(5,'Ozonioterapia','Aplicação de ozônio para fins estéticos','Facial',150.00,NULL,30,1,'Ozonioterapia','Alergia ao ozônio; deficiência de G6PD',0,'Ativo'),(6,'Limpeza de Pele Profunda','Extração e higienização profunda da pele do rosto','Facial',160.00,NULL,60,0,NULL,'Lesões ativas de pele; infecções cutâneas',0,'Ativo'),(7,'Drenagem Linfática','Massagem manual para redução de retenção de líquido','Corporal',140.00,NULL,50,0,NULL,'Trombose; infecções ativas',0,'Ativo'),(8,'Peeling Facial','Renovação celular da pele do rosto','Facial',180.00,NULL,40,0,NULL,'Gestantes; exposição solar recente; uso de ácidos no mesmo período',0,'Ativo'),(9,'Hidratação Facial','massagem relaxante com creme hidratante apos limpeza facial','Facial',80.00,20.00,30,0,NULL,'nenhuma',0,'Ativo'),(10,'Botox','uma ampola','facial',250.00,20.00,40,0,NULL,NULL,1,'Ativo'),(11,'Bioestimulador de Colageno',NULL,'facial',1000.00,35.00,60,0,NULL,NULL,1,'Ativo');


-- Dump completed on 2026-10-25 15:08:34


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `profissional_procedimento`
--

DROP TABLE IF EXISTS `profissional_procedimento`;
CREATE TABLE `profissional_procedimento` (
  `id_profissional` int NOT NULL,
  `id_procedimento` int NOT NULL,
  PRIMARY KEY (`id_profissional`,`id_procedimento`),
  CONSTRAINT `fk_pp_procedimento` FOREIGN KEY (`id_procedimento`) REFERENCES `procedimento` (`id_procedimento`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_pp_profissional` FOREIGN KEY (`id_profissional`) REFERENCES `profissional` (`id_profissional`) ON DELETE RESTRICT ON UPDATE CASCADE
);

--
-- Dumping data for table `profissional_procedimento`
--

INSERT INTO `profissional_procedimento` VALUES (1,1),(2,1),(3,1),(5,1),(1,2),(3,2),(4,2),(5,2),(1,3),(2,3),(3,3),(5,3),(1,4),(2,4),(3,4),(5,4),(1,5),(2,5),(3,5),(5,5),(1,6),(3,6),(4,6),(5,6),(1,7),(2,7),(3,7),(4,7),(1,8),(3,8),(4,8),(5,8),(1,9),(3,9),(4,9),(5,9),(1,10),(3,10),(5,10),(5,11);


-- Dump completed on 2026-10-25 15:08:35


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
CREATE TABLE `usuario` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(150) NOT NULL,
  `login` varchar(100) NOT NULL,
  `senha_hash` varchar(255) NOT NULL,
  `perfil_acesso` varchar(50) NOT NULL,
  `id_profissional` int DEFAULT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'Ativo',
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `uq_usuario_login` (`login`),
  UNIQUE KEY `uq_usuario_profissional` (`id_profissional`),
  CONSTRAINT `fk_usuario_profissional` FOREIGN KEY (`id_profissional`) REFERENCES `profissional` (`id_profissional`) ON DELETE RESTRICT ON UPDATE CASCADE
);

--
-- Dumping data for table `usuario`
--



-- Dump completed on 2026-10-25 15:08:35


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `paciente`
--

DROP TABLE IF EXISTS `paciente`;
CREATE TABLE `paciente` (
  `id_paciente` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(150) NOT NULL,
  `telefone` varchar(20) NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `data_nascimento` date NOT NULL,
  `cpf` varchar(14) NOT NULL,
  `endereco` varchar(255) DEFAULT NULL,
  `genero` varchar(30) DEFAULT NULL,
  `peso` decimal(5,2) DEFAULT NULL,
  `altura` int DEFAULT NULL,
  `alergias` text,
  `restricoes_saude` text,
  `preferencia_bebida` varchar(50) NOT NULL DEFAULT 'Sem preferência',
  `preferencia_profissional` varchar(50) NOT NULL DEFAULT 'I',
  `contato_emergencia_nome` varchar(150) DEFAULT NULL,
  `contato_emergencia_telefone` varchar(20) DEFAULT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'Ativo',
  `canal_origem` varchar(20) DEFAULT NULL,
  `observacoes` text,
  PRIMARY KEY (`id_paciente`),
  UNIQUE KEY `uq_paciente_cpf` (`cpf`)
);

--
-- Dumping data for table `paciente`
--

INSERT INTO `paciente` VALUES (1,'Fernanda Dias','(11) 98221-4410','fernanda.dias@email.com','1988-03-14','123.456.789-00','Rua das Acácias, 120 - São Paulo/SP','Feminino',68.50,165,'Dipirona','Uso relatado de isotretinoína','Chá','F','Roberto Dias','(11) 98888-1111','Inativo',NULL,NULL),(2,'Marina Alves','(11) 97744-2201','marina.alves@email.com','1992-07-22','234.567.890-11','Av. Paulista, 900 - São Paulo/SP','Feminino',61.00,160,NULL,NULL,'Café','I','Lucas Alves','(11) 97777-2222','Ativo',NULL,NULL),(3,'Camila Reis','(11) 98890-1123','camila.reis@email.com','1995-11-02','345.678.901-22','Rua Oscar Freire, 300 - São Paulo/SP','Feminino',58.20,162,NULL,NULL,'Chá','F','Ana Reis','(11) 96666-3333','Inativo',NULL,NULL),(4,'Patrícia Nunes','(11) 99213-7765','patricia.nunes@email.com','1985-01-30','456.789.012-33','Alameda Santos, 45 - São Paulo/SP','Feminino',70.00,168,NULL,NULL,'Sem preferência','I','Marcos Nunes','(11) 95555-4444','Ativo',NULL,NULL),(5,'Helena Martins','(11) 98123-0099','helena.martins@email.com','1990-09-18','567.890.123-44','Rua Haddock Lobo, 80 - São Paulo/SP','Feminino',64.00,170,NULL,NULL,'Café','M','Paulo Martins','(11) 94444-5555','Ativo',NULL,NULL),(6,'Beatriz Souza','(11) 98112-4432','beatriz.souza@email.com','1993-04-09','678.901.234-55','Rua Augusta, 500 - São Paulo/SP','Feminino',66.00,164,NULL,NULL,'Café','I','Igor Souza','(11) 93333-6666','Ativo',NULL,NULL),(7,'Juliana Castro','(11) 98556-7781','juliana.castro@email.com','1991-12-20','789.012.345-66','Rua Bela Cintra, 210 - São Paulo/SP','Feminino',59.50,158,NULL,NULL,'Chá','F','Pedro Castro','(11) 92222-7777','Ativo',NULL,NULL),(8,'Renata Lima','(11) 98774-1190','renata.lima@email.com','1987-06-11','890.123.456-77','Alameda Jaú, 77 - São Paulo/SP','Feminino',72.00,167,NULL,NULL,'Sem preferência','I','Carlos Lima','(11) 91111-8888','Ativo',NULL,NULL),(9,'Larissa Ferreira','(11) 98990-3321','larissa.ferreira@email.com','1996-02-28','901.234.567-88','Rua Consolação, 1500 - São Paulo/SP','Feminino',55.00,161,NULL,NULL,'Café','F','Fábio Ferreira','(11) 90000-9999','Ativo',NULL,NULL),(10,'Gabriela Santos','(11) 98332-6654','gabriela.santos@email.com','1989-10-05','012.345.678-99','Rua Teodoro Sampaio, 60 - São Paulo/SP','Feminino',63.00,163,NULL,NULL,'Chá','I','Diego Santos','(11) 99999-0000','Ativo',NULL,NULL),(11,'Maria Oliveira','11997325580','mariaoliveira@email.com','2000-09-30','529.982.247-25','rau Das Flores 71','feminino',71.00,170,'nimesulida',NULL,'Chá','I','Luiza Oliveira','11997486279','Ativo','Presencial',NULL),(12,'Ana Caroline Marques','11997834172','AnaMarques@email.com','2004-04-22','111.111.111-12','rua Brigadeiro Marcos Antonio 27','feminino',78.00,170,NULL,NULL,'Café','F','Cristina Marques','11987224180','Ativo','Instagram',NULL),(13,'Vitoria Beckran','11987654532','vitoriabec@mail.com','1997-10-15','111.786.354-09','rua dos Caracois 11','feminino',89.00,180,'Rivotril','Trombose na perna esquerda','Sem preferência','I','Jubileu','11976854356','Ativo','WhatsApp',NULL),(14,'Amanda Cardozo Linhares','1199995645','amandac@mail.com','2002-10-12','234.567.897-23','rua Amendoa 37','Feminino',90.00,177,'nenhuma','nehuma','Café','F','Bruna Cardozo','11923152120','Ativo','WhatsApp',NULL),(15,'Cecilia Martins','11923214782','cecilia@mail.com.br','2000-09-10','234.546.789-11','rua das Oliveiras 47','feminino',70.00,160,'nenhuma','nehuma','Chá','F','maria martins','11987654568','Ativo','Instagram',NULL),(16,'Olivia Oliveira','11987675423','Oliviamaria@mail.com','2002-04-23','234.567.876-12','rua Morango Azul 03','feminino',69.00,167,'nenhuma','nehuma','Chá','M','Ana Paula Oliveira','11976548976','Ativo','WhatsApp',NULL),(17,'Mariane Silva','11987654987','mariane@mail.com','1990-05-10','234.564.786-90','rua Canas de Acucar 32','feminino',108.00,165,'nenhuma','nenhuma','Chá','I','Luana Silva','11987564532','Ativo','Instagram',NULL),(18,'Ana Beatriz Ramos','(11) 97712-4011','ana.ramos@email.com','1991-03-14','728.415.903-05','Rua Fradique Couto, 210 - São Paulo/SP','Feminino',61.50,163,NULL,NULL,'Café','F','Marcos Ramos','(11) 97712-4000','Ativo','WhatsApp',NULL),(19,'Bruno Almeida Costa','(11) 98831-2245','bruno.costa@email.com','1988-07-22','356.092.184-89','Av. Angélica, 1050 - São Paulo/SP','Masculino',84.20,178,NULL,NULL,'Café','M','Sônia Costa','(11) 98831-2200','Ativo','Indicação',NULL),(20,'Carla Mendes Pinto','(11) 96622-8810','carla.pinto@email.com','1995-11-02','914.730.526-61','Rua Harmonia, 88 - São Paulo/SP','Feminino',57.80,160,'Dipirona',NULL,'Chá','F','Luiza Pinto','(11) 96622-8800','Ativo','Instagram',NULL),(21,'Danielle Ferreira Rocha','(11) 97400-5233','danielle.rocha@email.com','1990-01-30','402.681.579-01','Rua Cotoxó, 303 - São Paulo/SP','Feminino',66.10,167,NULL,'Diabetes tipo 2 controlada','Sem preferência','I','Paulo Rocha','(11) 97400-5200','Inativo','Presencial',NULL),(22,'Eduardo Nascimento Lima','(11) 98122-7015','eduardo.lima@email.com','1984-05-18','581.324.097-79','Rua Girassol, 720 - São Paulo/SP','Masculino',92.40,182,NULL,NULL,'Café','M','Renata Lima','(11) 98122-7000','Ativo','WhatsApp',NULL),(23,'Fabíola Duarte Melo','(11) 96555-8801','fabiola.melo@email.com','1997-09-08','167.840.253-26','Av. Rebouças, 2200 - São Paulo/SP','Feminino',54.30,158,'Penicilina',NULL,'Chá','F','Cecília Melo','(11) 96555-8800','Ativo','Instagram',NULL),(24,'Gisele Campos Freitas','(11) 97233-1907','gisele.freitas@email.com','1982-12-11','829.513.746-82','Rua Aspicuelta, 145 - São Paulo/SP','Feminino',71.90,170,NULL,NULL,'Café','F','Diego Freitas','(11) 97233-1900','Ativo','Indicação',NULL),(25,'Hugo Barbosa Pires','(11) 96011-4478','hugo.pires@email.com','1979-06-25','635.208.419-24','Rua Teodoro Sampaio, 810 - São Paulo/SP','Masculino',96.70,185,NULL,'Hipertensão controlada','Sem preferência','M','Ana Pires','(11) 96011-4400','Inativo','Presencial',NULL),(26,'Isabela Cristina Moreira','(11) 98450-7712','isabela.moreira@email.com','2001-04-19','741.089.365-66','Rua Vupabussu, 320 - São Paulo/SP','Feminino',59.60,165,NULL,NULL,'Chá','F','Silvia Moreira','(11) 98450-7700','Ativo','WhatsApp',NULL),(27,'Jéssica Araújo Bastos','(11) 97123-6604','jessica.bastos@email.com','1993-08-27','203.571.846-53','Rua Harmonia, 977 - São Paulo/SP','Feminino',63.20,161,'Látex',NULL,'Café','I','Tiago Bastos','(11) 97123-6600','Ativo','Instagram',NULL),(28,'Karina Lopes Furtado','(11) 99344-1208','karina.furtado@email.com','1986-02-05','958.614.273-64','Av. Paulista, 1500 - São Paulo/SP','Feminino',68.40,168,NULL,NULL,'Sem preferência','F','Marcia Lopes','(11) 99344-1200','Ativo','Presencial',NULL),(29,'Lucas Martins Correia','(11) 98765-2231','lucas.correia@email.com','1992-10-16','470.396.812-69','Rua dos Pinheiros, 640 - São Paulo/SP','Masculino',78.90,176,NULL,NULL,'Café','M','Helena Correia','(11) 98765-2200','Ativo','WhatsApp',NULL),(30,'Mariana Esteves Rangel','(11) 96688-3315','mariana.rangel@email.com','1998-05-23','516.842.370-17','Rua Artur de Azevedo, 178 - São Paulo/SP','Feminino',56.10,159,NULL,NULL,'Chá','F','Rafael Rangel','(11) 96688-3300','Ativo','Instagram',NULL),(31,'Natália Pires Bittencourt','(11) 97321-9088','natalia.bittencourt@email.com','1985-07-09','834.920.751-50','Rua Cardeal Arcoverde, 990 - São Paulo/SP','Feminino',74.80,171,'Nimesulida','Uso de anticoagulante','Café','I','Otávio Bittencourt','(11) 97321-9000','Inativo','Indicação',NULL),(32,'Otávio Ramos Teles','(11) 96122-5540','otavio.teles@email.com','1980-03-03','290.715.364-16','Av. Pompeia, 480 - São Paulo/SP','Masculino',88.30,180,NULL,NULL,'Sem preferência','M','Laura Teles','(11) 96122-5500','Ativo','Presencial',NULL),(33,'Priscila Gomes Sampaio','(11) 98566-7714','priscila.sampaio@email.com','1996-12-29','607.158.294-67','Rua Wisard, 205 - São Paulo/SP','Feminino',61.70,164,NULL,NULL,'Chá','F','Bruna Sampaio','(11) 98566-7700','Ativo','WhatsApp',NULL),(34,'Rafael Cardoso Brito','(11) 97045-6612','rafael.brito@email.com','1989-09-13','173.849.502-79','Rua da Consolação, 2300 - São Paulo/SP','Masculino',82.50,179,NULL,NULL,'Café','M','Paula Brito','(11) 97045-6600','Ativo','Instagram',NULL),(35,'Sabrina Alves Nogueira','(11) 98411-2036','sabrina.nogueira@email.com','2000-02-21','749.213.586-64','Rua Girassol, 310 - São Paulo/SP','Feminino',65.90,169,NULL,NULL,'Sem preferência','I','Vera Nogueira','(11) 98411-2000','Ativo','Presencial',NULL),(36,'Tatiane Sousa Macedo','(11) 97788-4520','tatiane.macedo@email.com','1994-06-07','382.604.917-96','Av. Brig. Faria Lima, 1200 - São Paulo/SP','Feminino',70.20,166,'AAS',NULL,'Café','F','Igor Macedo','(11) 97788-4500','Ativo','Indicação',NULL),(37,'Vinícius Dias Queiroz','(11) 96233-8841','vinicius.queiroz@email.com','1983-11-26','925.370.148-04','Rua Heitor Penteado, 560 - São Paulo/SP','Masculino',94.10,184,NULL,'Hérnia de disco em tratamento','Chá','M','Sandra Queiroz','(11) 96233-8800','Inativo','WhatsApp',NULL),(38,'Wanda Ribeiro Peixoto','(11) 98100-7732','wanda.peixoto@email.com','1976-04-15','468.091.725-30','Rua Belmiro Braga, 190 - São Paulo/SP','Feminino',76.40,162,NULL,NULL,'Chá','F','Cláudia Peixoto','(11) 98100-7700','Ativo','Presencial',NULL),(39,'Yara Custódio Fontoura','(11) 96745-9903','yara.fontoura@email.com','1999-08-31','537.628.140-07','Rua Fradique Couto, 115 - São Paulo/SP','Feminino',58.40,157,NULL,NULL,'Café','F','Márcio Fontoura','(11) 96745-9900','Ativo','Instagram',NULL),(40,'Alessandra Vieira Menezes','(11) 97899-1408','alessandra.menezes@email.com','1987-01-12','814.250.639-42','Av. Europa, 640 - São Paulo/SP','Feminino',72.60,173,'dipirona',NULL,'Sem preferência','I','Renato Menezes','(11) 97899-1400','Ativo','WhatsApp',NULL),(41,'Bernardo Oliveira Tavares','(11) 96322-4075','bernardo.tavares@email.com','1981-10-04','296.741.058-67','Rua Capote Valente, 520 - São Paulo/SP','Masculino',90.80,188,NULL,NULL,'Café','M','Elena Tavares','(11) 96322-4000','Ativo','Indicação',NULL),(42,'Clarice Nogueira Vasques','(11) 98544-6629','clarice.vasques@email.com','1990-05-28','651.809.374-93','Rua Purpurina, 410 - São Paulo/SP','Feminino',62.30,160,'Iodo',NULL,'Chá','F','Fábio Vasques','(11) 98544-6600','Ativo','Instagram',NULL),(43,'Diego Fernandes Salles','(11) 97466-1102','diego.salles@email.com','1993-12-17','407.586.231-35','Rua dos Trilhos, 85 - São Paulo/SP','Masculino',79.60,175,NULL,NULL,'Sem preferência','I','Nádia Salles','(11) 97466-1100','Ativo','Presencial',NULL),(44,'Fernanda Luz Carvalho','(11) 98700-5518','fernanda.carvalho@email.com','1997-03-25','760.134.895-48','Av. Pacaembu, 760 - São Paulo/SP','Feminino',55.70,164,NULL,NULL,'Café','F','Túlio Carvalho','(11) 98700-5500','Ativo','WhatsApp',NULL),(45,'Gustavo Henrique Prates','(11) 96188-9924','gustavo.prates@email.com','1978-08-06','528.903.614-89','Rua Harmonia, 1440 - São Paulo/SP','Masculino',87.20,181,NULL,NULL,'Chá','M','Regina Prates','(11) 96188-9900','Ativo','Presencial',NULL),(46,'Letícia Barreto Camargo','(11) 97533-2207','leticia.camargo@email.com','2002-06-20','183.406.927-04','Rua Funchal, 225 - São Paulo/SP','Feminino',60.90,168,NULL,NULL,'Café','F','Adriana Camargo','(11) 97533-2200','Ativo','Instagram',NULL),(47,'Renato Aguiar Sodré','(11) 98022-6619','renato.sodre@email.com','1986-09-09','869.250.471-85','Rua Groenlândia, 330 - São Paulo/SP','Masculino',85.10,177,NULL,NULL,'Sem preferência','I','Camila Sodré','(11) 98022-6600','Ativo','Indicação',NULL);


-- Dump completed on 2026-10-25 15:08:35


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `anamnese`
--

DROP TABLE IF EXISTS `anamnese`;
CREATE TABLE `anamnese` (
  `id_anamnese` int NOT NULL AUTO_INCREMENT,
  `id_paciente` int NOT NULL,
  `data_preenchimento` date NOT NULL,
  `alergias` text,
  `medicacoes_continuas` text,
  `cirurgias_previas` text,
  `gestante_ou_lactante` tinyint(1) NOT NULL DEFAULT '0',
  `uso_isotretinoina` tinyint(1) NOT NULL DEFAULT '0',
  `historico_queloide` tinyint(1) NOT NULL DEFAULT '0',
  `doencas_de_pele` text,
  `contraindicacoes_identificadas` text,
  PRIMARY KEY (`id_anamnese`),
  UNIQUE KEY `uq_anamnese_paciente` (`id_paciente`),
  CONSTRAINT `fk_anamnese_paciente` FOREIGN KEY (`id_paciente`) REFERENCES `paciente` (`id_paciente`) ON DELETE RESTRICT ON UPDATE CASCADE
);

--
-- Dumping data for table `anamnese`
--

INSERT INTO `anamnese` VALUES (1,1,'2026-07-01','Dipirona','Isotretinoína (tratamento dermatológico em curso)',NULL,0,1,0,NULL,'Depilação a laser'),(2,2,'2026-06-15',NULL,NULL,NULL,0,0,0,NULL,NULL),(3,3,'2026-06-20',NULL,NULL,NULL,0,0,0,NULL,NULL),(4,4,'2026-07-10',NULL,NULL,'Cirurgia bariátrica (2022)',0,0,0,NULL,NULL),(5,5,'2026-05-05',NULL,NULL,NULL,0,0,0,NULL,NULL),(6,6,'2026-08-18',NULL,NULL,NULL,0,0,0,NULL,NULL),(7,7,'2026-08-14',NULL,NULL,NULL,0,0,0,NULL,NULL),(8,8,'2026-08-27',NULL,NULL,NULL,0,0,0,NULL,NULL),(9,9,'2026-08-29',NULL,NULL,NULL,0,0,0,NULL,NULL),(10,10,'2026-08-24',NULL,NULL,NULL,0,0,0,NULL,NULL),(11,14,'2026-10-21','paracetamol','nenhuma','não',0,0,0,'não','não');


-- Dump completed on 2026-10-25 15:08:34


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `prontuario`
--

DROP TABLE IF EXISTS `prontuario`;
CREATE TABLE `prontuario` (
  `id_prontuario` int NOT NULL AUTO_INCREMENT,
  `id_paciente` int NOT NULL,
  `data_criacao` date NOT NULL,
  PRIMARY KEY (`id_prontuario`),
  UNIQUE KEY `uq_prontuario_paciente` (`id_paciente`),
  CONSTRAINT `fk_prontuario_paciente` FOREIGN KEY (`id_paciente`) REFERENCES `paciente` (`id_paciente`) ON DELETE RESTRICT ON UPDATE CASCADE
);

--
-- Dumping data for table `prontuario`
--

INSERT INTO `prontuario` VALUES (1,1,'2026-07-01'),(2,2,'2026-06-15'),(3,3,'2026-06-20'),(4,4,'2026-07-10'),(5,5,'2026-05-05'),(6,6,'2026-08-18'),(7,7,'2026-08-14'),(8,8,'2026-08-27'),(9,9,'2026-08-29'),(10,10,'2026-08-24'),(11,20,'2026-10-17'),(12,16,'2026-10-21');


-- Dump completed on 2026-10-25 15:08:34


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `pacote`
--

DROP TABLE IF EXISTS `pacote`;
CREATE TABLE `pacote` (
  `id_pacote` int NOT NULL AUTO_INCREMENT,
  `nome` varchar(150) NOT NULL,
  `descricao` text,
  `valor_total` decimal(10,2) NOT NULL,
  `validade_dias` int NOT NULL DEFAULT '365',
  `status` varchar(50) NOT NULL DEFAULT 'Ativo',
  PRIMARY KEY (`id_pacote`)
);

--
-- Dumping data for table `pacote`
--

INSERT INTO `pacote` VALUES (1,'Verão 2027','Pacote de perda de gordura: criolipólise + drenagem linfática',3400.00,365,'Ativo'),(2,'Corpo em Forma','Pacote de perda de gordura: carboxiterapia + criolipólise',3700.00,365,'Ativo'),(3,'Pele Perfeita','Pacote facial: limpeza de pele profunda + peeling facial',2450.00,365,'Ativo'),(4,'Glow Facial','Pacote facial: ozonioterapia + radiofrequência facial',2850.00,365,'Ativo'),(5,'Pernas Lisas','Pacote de depilação a laser para pernas completas',1950.00,365,'Ativo'),(6,'Verao perfeito','criolipolise\ndrenagem \nradiofrequencia',3800.00,365,'Ativo'),(7,'Rosto Perfeito','Pacote para processos faciais',680.00,60,'Ativo'),(8,'Glow Facil Plus',NULL,2300.00,365,'Ativo');


-- Dump completed on 2026-10-25 15:08:33


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `item_pacote`
--

DROP TABLE IF EXISTS `item_pacote`;
CREATE TABLE `item_pacote` (
  `id_item_pacote` int NOT NULL AUTO_INCREMENT,
  `id_pacote` int NOT NULL,
  `id_procedimento` int NOT NULL,
  `numero_sessoes` int NOT NULL,
  `periodicidade_dias` int DEFAULT NULL,
  PRIMARY KEY (`id_item_pacote`),
  UNIQUE KEY `uq_itempacote_pacote_procedimento` (`id_pacote`,`id_procedimento`),
  CONSTRAINT `fk_itempacote_pacote` FOREIGN KEY (`id_pacote`) REFERENCES `pacote` (`id_pacote`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_itempacote_procedimento` FOREIGN KEY (`id_procedimento`) REFERENCES `procedimento` (`id_procedimento`) ON DELETE RESTRICT ON UPDATE CASCADE
);

--
-- Dumping data for table `item_pacote`
--

INSERT INTO `item_pacote` VALUES (1,1,1,10,30),(2,1,7,10,15),(3,2,4,10,15),(4,2,1,8,30),(5,3,6,8,30),(6,3,8,8,15),(7,4,5,10,15),(8,4,3,8,30),(9,5,2,10,30),(10,7,10,1,NULL),(11,7,9,2,NULL),(12,7,6,2,NULL),(13,8,11,1,NULL),(14,8,10,2,NULL),(15,8,9,1,NULL),(16,8,6,1,NULL),(17,6,5,4,NULL),(18,6,1,1,NULL),(19,6,7,2,NULL),(20,6,3,4,NULL);


-- Dump completed on 2026-10-25 15:08:34


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `agendamento`
--

DROP TABLE IF EXISTS `agendamento`;
CREATE TABLE `agendamento` (
  `id_agendamento` int NOT NULL AUTO_INCREMENT,
  `id_paciente` int NOT NULL,
  `id_procedimento` int NOT NULL,
  `id_profissional` int NOT NULL,
  `id_sala` int NOT NULL,
  `id_equipamento` int DEFAULT NULL,
  `data_hora` datetime NOT NULL,
  `duracao_minutos` int NOT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'Agendado',
  `tipo_cancelamento` varchar(50) DEFAULT NULL,
  `anexo_justificativa_medica` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id_agendamento`),
  CONSTRAINT `fk_agendamento_equipamento` FOREIGN KEY (`id_equipamento`) REFERENCES `equipamento` (`id_equipamento`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_agendamento_paciente` FOREIGN KEY (`id_paciente`) REFERENCES `paciente` (`id_paciente`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_agendamento_procedimento` FOREIGN KEY (`id_procedimento`) REFERENCES `procedimento` (`id_procedimento`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_agendamento_profissional` FOREIGN KEY (`id_profissional`) REFERENCES `profissional` (`id_profissional`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_agendamento_sala` FOREIGN KEY (`id_sala`) REFERENCES `sala` (`id_sala`) ON DELETE RESTRICT ON UPDATE CASCADE
);

--
-- Dumping data for table `agendamento`
--

INSERT INTO `agendamento` VALUES (1,1,3,2,3,4,'2026-08-10 09:00:00',45,'Realizado',NULL,NULL),(2,1,3,2,3,4,'2026-08-24 09:00:00',45,'Realizado',NULL,NULL),(3,2,1,1,1,1,'2026-08-31 09:00:00',45,'Cancelado','Com débito',NULL),(4,3,5,2,3,3,'2026-08-31 10:00:00',30,'Realizado',NULL,NULL),(5,4,3,3,3,4,'2026-08-31 11:00:00',45,'Faltou',NULL,NULL),(6,5,2,3,2,2,'2026-07-15 14:00:00',30,'Realizado',NULL,NULL),(7,6,4,3,1,5,'2026-08-31 10:00:00',40,'Realizado',NULL,NULL),(8,7,6,1,1,NULL,'2026-08-31 11:00:00',60,'Faltou',NULL,NULL),(9,8,7,1,3,NULL,'2026-08-31 09:00:00',50,'Realizado',NULL,NULL),(10,9,8,4,2,NULL,'2026-08-31 10:00:00',40,'Realizado',NULL,NULL),(11,11,4,3,1,5,'2026-10-16 12:00:00',40,'Realizado',NULL,NULL),(12,3,1,1,3,1,'2026-10-17 09:00:00',120,'Agendado',NULL,NULL),(13,16,3,3,2,4,'2026-10-16 13:00:00',45,'Agendado',NULL,NULL),(14,14,10,5,1,NULL,'2026-10-17 11:30:00',40,'Agendado',NULL,NULL),(15,12,4,1,2,5,'2026-10-17 11:30:00',40,'Agendado',NULL,NULL),(16,6,4,3,3,5,'2026-10-17 15:30:00',40,'Realizado',NULL,NULL),(17,14,2,1,3,6,'2026-10-17 17:00:00',60,'Agendado',NULL,NULL),(18,9,5,1,2,3,'2026-10-17 11:00:00',30,'Agendado',NULL,NULL),(19,16,6,3,1,NULL,'2026-12-22 09:00:00',60,'Agendado',NULL,NULL),(20,5,6,3,3,NULL,'2026-12-19 09:00:00',60,'Agendado',NULL,NULL),(21,45,6,3,2,NULL,'2026-12-18 09:00:00',60,'Agendado',NULL,NULL),(22,41,6,3,1,NULL,'2026-12-17 09:00:00',60,'Agendado',NULL,NULL),(23,37,6,3,3,NULL,'2026-12-16 09:00:00',60,'Agendado',NULL,NULL),(24,34,6,3,2,NULL,'2026-12-15 09:00:00',60,'Agendado',NULL,NULL),(25,32,6,3,1,NULL,'2026-12-12 09:00:00',60,'Agendado',NULL,NULL),(26,29,6,3,3,NULL,'2026-12-11 09:00:00',60,'Agendado',NULL,NULL),(27,25,6,3,2,NULL,'2026-12-10 09:00:00',60,'Agendado',NULL,NULL),(28,22,6,3,1,NULL,'2026-12-09 09:00:00',60,'Agendado',NULL,NULL),(29,19,6,3,3,NULL,'2026-12-08 09:00:00',60,'Agendado',NULL,NULL),(30,16,6,3,2,NULL,'2026-12-05 09:00:00',60,'Agendado',NULL,NULL),(31,5,6,3,1,NULL,'2026-12-04 09:00:00',60,'Agendado',NULL,NULL),(32,45,6,3,3,NULL,'2026-12-03 09:00:00',60,'Agendado',NULL,NULL),(33,41,6,3,2,NULL,'2026-12-02 09:00:00',60,'Agendado',NULL,NULL),(34,37,6,3,1,NULL,'2026-12-01 09:00:00',60,'Agendado',NULL,NULL),(35,34,6,3,3,NULL,'2026-11-28 09:00:00',60,'Agendado',NULL,NULL),(36,32,6,3,2,NULL,'2026-11-27 09:00:00',60,'Agendado',NULL,NULL),(37,29,6,3,1,NULL,'2026-11-26 09:00:00',60,'Agendado',NULL,NULL),(38,25,6,3,3,NULL,'2026-11-25 09:00:00',60,'Agendado',NULL,NULL),(39,22,6,3,2,NULL,'2026-11-24 09:00:00',60,'Agendado',NULL,NULL),(40,19,6,3,1,NULL,'2026-11-21 09:00:00',60,'Agendado',NULL,NULL),(41,16,6,3,3,NULL,'2026-11-20 09:00:00',60,'Agendado',NULL,NULL),(42,5,6,3,2,NULL,'2026-11-19 09:00:00',60,'Agendado',NULL,NULL),(43,45,6,3,1,NULL,'2026-11-18 09:00:00',60,'Agendado',NULL,NULL),(44,41,6,3,3,NULL,'2026-11-17 09:00:00',60,'Agendado',NULL,NULL),(45,37,6,3,2,NULL,'2026-11-14 09:00:00',60,'Agendado',NULL,NULL),(46,34,6,3,1,NULL,'2026-11-13 09:00:00',60,'Agendado',NULL,NULL),(47,32,6,3,3,NULL,'2026-11-12 09:00:00',60,'Agendado',NULL,NULL),(48,29,6,3,2,NULL,'2026-11-11 09:00:00',60,'Agendado',NULL,NULL),(49,25,6,3,1,NULL,'2026-11-10 09:00:00',60,'Agendado',NULL,NULL),(50,22,6,3,3,NULL,'2026-11-07 09:00:00',60,'Agendado',NULL,NULL),(51,19,6,3,2,NULL,'2026-11-06 09:00:00',60,'Agendado',NULL,NULL),(52,16,6,3,1,NULL,'2026-11-05 09:00:00',60,'Agendado',NULL,NULL),(53,5,6,3,3,NULL,'2026-11-04 09:00:00',60,'Agendado',NULL,NULL),(54,45,6,3,2,NULL,'2026-11-03 09:00:00',60,'Agendado',NULL,NULL),(55,41,6,3,1,NULL,'2026-10-31 09:00:00',60,'Agendado',NULL,NULL),(56,37,6,3,3,NULL,'2026-10-30 09:00:00',60,'Agendado',NULL,NULL),(57,34,6,3,2,NULL,'2026-10-29 09:00:00',60,'Agendado',NULL,NULL),(58,32,6,3,1,NULL,'2026-10-28 09:00:00',60,'Agendado',NULL,NULL),(59,29,6,3,3,NULL,'2026-10-27 09:00:00',60,'Agendado',NULL,NULL),(60,25,6,3,2,NULL,'2026-10-24 09:00:00',60,'Agendado',NULL,NULL),(61,22,6,3,1,NULL,'2026-10-23 09:00:00',60,'Agendado',NULL,NULL),(62,19,6,3,3,NULL,'2026-10-22 09:00:00',60,'Agendado',NULL,NULL),(63,16,6,3,2,NULL,'2026-10-21 09:00:00',60,'Realizado',NULL,NULL),(64,5,6,3,1,NULL,'2026-10-20 09:00:00',60,'Faltou',NULL,NULL),(65,9,7,4,2,NULL,'2026-12-22 11:00:00',50,'Agendado',NULL,NULL),(66,7,7,1,1,NULL,'2026-12-19 11:00:00',50,'Agendado',NULL,NULL),(67,3,7,4,3,NULL,'2026-12-18 11:00:00',50,'Agendado',NULL,NULL),(68,1,7,1,2,NULL,'2026-12-17 11:00:00',50,'Agendado',NULL,NULL),(69,46,7,4,1,NULL,'2026-12-16 11:00:00',50,'Agendado',NULL,NULL),(70,44,7,1,3,NULL,'2026-12-15 11:00:00',50,'Agendado',NULL,NULL),(71,42,7,4,2,NULL,'2026-12-12 11:00:00',50,'Agendado',NULL,NULL),(72,39,7,1,1,NULL,'2026-12-11 11:00:00',50,'Agendado',NULL,NULL),(73,38,7,4,3,NULL,'2026-12-10 11:00:00',50,'Agendado',NULL,NULL),(74,36,7,1,2,NULL,'2026-12-09 11:00:00',50,'Agendado',NULL,NULL),(75,33,7,4,1,NULL,'2026-12-08 11:00:00',50,'Agendado',NULL,NULL),(76,30,7,1,3,NULL,'2026-12-05 11:00:00',50,'Agendado',NULL,NULL),(77,28,7,4,2,NULL,'2026-12-04 11:00:00',50,'Agendado',NULL,NULL),(78,26,7,1,1,NULL,'2026-12-03 11:00:00',50,'Agendado',NULL,NULL),(79,24,7,4,3,NULL,'2026-12-02 11:00:00',50,'Agendado',NULL,NULL),(80,23,7,1,2,NULL,'2026-12-01 11:00:00',50,'Agendado',NULL,NULL),(81,20,7,4,1,NULL,'2026-11-28 11:00:00',50,'Agendado',NULL,NULL),(82,18,7,1,3,NULL,'2026-11-27 11:00:00',50,'Agendado',NULL,NULL),(83,15,7,4,2,NULL,'2026-11-26 11:00:00',50,'Agendado',NULL,NULL),(84,14,7,1,1,NULL,'2026-11-25 11:00:00',50,'Agendado',NULL,NULL),(85,12,7,4,3,NULL,'2026-11-24 11:00:00',50,'Agendado',NULL,NULL),(86,9,7,1,2,NULL,'2026-11-21 11:00:00',50,'Agendado',NULL,NULL),(87,7,7,4,1,NULL,'2026-11-20 11:00:00',50,'Agendado',NULL,NULL),(88,3,7,1,3,NULL,'2026-11-19 11:00:00',50,'Agendado',NULL,NULL),(89,1,7,4,2,NULL,'2026-11-18 11:00:00',50,'Agendado',NULL,NULL),(90,46,7,1,1,NULL,'2026-11-17 11:00:00',50,'Agendado',NULL,NULL),(91,44,7,4,3,NULL,'2026-11-14 11:00:00',50,'Agendado',NULL,NULL),(92,42,7,1,2,NULL,'2026-11-13 11:00:00',50,'Agendado',NULL,NULL),(93,39,7,4,1,NULL,'2026-11-12 11:00:00',50,'Agendado',NULL,NULL),(94,38,7,1,3,NULL,'2026-11-11 11:00:00',50,'Agendado',NULL,NULL),(95,36,7,4,2,NULL,'2026-11-10 11:00:00',50,'Agendado',NULL,NULL),(96,33,7,1,1,NULL,'2026-11-07 11:00:00',50,'Agendado',NULL,NULL),(97,30,7,4,3,NULL,'2026-11-06 11:00:00',50,'Agendado',NULL,NULL),(98,28,7,1,2,NULL,'2026-11-05 11:00:00',50,'Agendado',NULL,NULL),(99,26,7,4,1,NULL,'2026-11-04 11:00:00',50,'Agendado',NULL,NULL),(100,24,7,1,3,NULL,'2026-11-03 11:00:00',50,'Agendado',NULL,NULL),(101,23,7,4,2,NULL,'2026-10-31 11:00:00',50,'Agendado',NULL,NULL),(102,20,7,1,1,NULL,'2026-10-30 11:00:00',50,'Agendado',NULL,NULL),(103,18,7,4,3,NULL,'2026-10-29 11:00:00',50,'Agendado',NULL,NULL),(104,15,7,1,2,NULL,'2026-10-28 11:00:00',50,'Agendado',NULL,NULL),(105,14,7,4,1,NULL,'2026-10-27 11:00:00',50,'Agendado',NULL,NULL),(106,12,7,1,3,NULL,'2026-10-24 11:00:00',50,'Agendado',NULL,NULL),(107,9,7,4,2,NULL,'2026-10-23 11:00:00',50,'Agendado',NULL,NULL),(108,7,7,1,1,NULL,'2026-10-22 11:00:00',50,'Agendado',NULL,NULL),(109,3,7,4,3,NULL,'2026-10-21 11:00:00',50,'Realizado',NULL,NULL),(110,1,7,1,2,NULL,'2026-10-20 11:00:00',50,'Confirmado',NULL,NULL),(111,2,9,4,3,NULL,'2026-12-22 13:00:00',30,'Agendado',NULL,NULL),(112,47,8,4,2,NULL,'2026-12-19 13:00:00',40,'Agendado',NULL,NULL),(113,43,9,4,1,NULL,'2026-12-18 13:00:00',30,'Agendado',NULL,NULL),(114,40,8,4,3,NULL,'2026-12-17 13:00:00',40,'Agendado',NULL,NULL),(115,35,9,4,2,NULL,'2026-12-16 13:00:00',30,'Agendado',NULL,NULL),(116,31,8,4,1,NULL,'2026-12-15 13:00:00',40,'Agendado',NULL,NULL),(117,27,9,4,3,NULL,'2026-12-12 13:00:00',30,'Agendado',NULL,NULL),(118,21,8,4,2,NULL,'2026-12-11 13:00:00',40,'Agendado',NULL,NULL),(119,17,9,4,1,NULL,'2026-12-10 13:00:00',30,'Agendado',NULL,NULL),(120,13,8,4,3,NULL,'2026-12-09 13:00:00',40,'Agendado',NULL,NULL),(121,11,9,4,2,NULL,'2026-12-08 13:00:00',30,'Agendado',NULL,NULL),(122,10,8,4,1,NULL,'2026-12-05 13:00:00',40,'Agendado',NULL,NULL),(123,8,9,4,3,NULL,'2026-12-04 13:00:00',30,'Agendado',NULL,NULL),(124,6,8,4,2,NULL,'2026-12-03 13:00:00',40,'Agendado',NULL,NULL),(125,4,9,4,1,NULL,'2026-12-02 13:00:00',30,'Agendado',NULL,NULL),(126,2,8,4,3,NULL,'2026-12-01 13:00:00',40,'Agendado',NULL,NULL),(127,47,9,4,2,NULL,'2026-11-28 13:00:00',30,'Agendado',NULL,NULL),(128,43,8,4,1,NULL,'2026-11-27 13:00:00',40,'Agendado',NULL,NULL),(129,40,9,4,3,NULL,'2026-11-26 13:00:00',30,'Agendado',NULL,NULL),(130,35,8,4,2,NULL,'2026-11-25 13:00:00',40,'Agendado',NULL,NULL),(131,31,9,4,1,NULL,'2026-11-24 13:00:00',30,'Agendado',NULL,NULL),(132,27,8,4,3,NULL,'2026-11-21 13:00:00',40,'Agendado',NULL,NULL),(133,21,9,4,2,NULL,'2026-11-20 13:00:00',30,'Agendado',NULL,NULL),(134,17,8,4,1,NULL,'2026-11-19 13:00:00',40,'Agendado',NULL,NULL),(135,13,9,4,3,NULL,'2026-11-18 13:00:00',30,'Agendado',NULL,NULL),(136,11,8,4,2,NULL,'2026-11-17 13:00:00',40,'Agendado',NULL,NULL),(137,10,9,4,1,NULL,'2026-11-14 13:00:00',30,'Agendado',NULL,NULL),(138,8,8,4,3,NULL,'2026-11-13 13:00:00',40,'Agendado',NULL,NULL),(139,6,9,4,2,NULL,'2026-11-12 13:00:00',30,'Agendado',NULL,NULL),(140,4,8,4,1,NULL,'2026-11-11 13:00:00',40,'Agendado',NULL,NULL),(141,2,9,4,3,NULL,'2026-11-10 13:00:00',30,'Agendado',NULL,NULL),(142,47,8,4,2,NULL,'2026-11-07 13:00:00',40,'Agendado',NULL,NULL),(143,43,9,4,1,NULL,'2026-11-06 13:00:00',30,'Agendado',NULL,NULL),(144,40,8,4,3,NULL,'2026-11-05 13:00:00',40,'Agendado',NULL,NULL),(145,35,9,4,2,NULL,'2026-11-04 13:00:00',30,'Agendado',NULL,NULL),(146,31,8,4,1,NULL,'2026-11-03 13:00:00',40,'Agendado',NULL,NULL),(147,27,9,4,3,NULL,'2026-10-31 13:00:00',30,'Agendado',NULL,NULL),(148,21,8,4,2,NULL,'2026-10-30 13:00:00',40,'Agendado',NULL,NULL),(149,17,9,4,1,NULL,'2026-10-29 13:00:00',30,'Agendado',NULL,NULL),(150,13,8,4,3,NULL,'2026-10-28 13:00:00',40,'Agendado',NULL,NULL),(151,11,9,4,2,NULL,'2026-10-27 13:00:00',30,'Agendado',NULL,NULL),(152,10,8,4,1,NULL,'2026-10-24 13:00:00',40,'Agendado',NULL,NULL),(153,8,9,4,3,NULL,'2026-10-23 13:00:00',30,'Agendado',NULL,NULL),(154,6,8,4,2,NULL,'2026-10-22 13:00:00',40,'Agendado',NULL,NULL),(155,4,9,4,1,NULL,'2026-10-21 13:00:00',30,'Realizado',NULL,NULL),(156,2,8,4,3,NULL,'2026-10-20 13:00:00',40,'Confirmado',NULL,NULL),(157,12,6,4,1,NULL,'2026-12-22 15:00:00',60,'Agendado',NULL,NULL),(158,9,9,1,3,NULL,'2026-12-19 15:00:00',30,'Agendado',NULL,NULL),(159,7,6,4,2,NULL,'2026-12-18 15:00:00',60,'Agendado',NULL,NULL),(160,3,9,1,1,NULL,'2026-12-17 15:00:00',30,'Agendado',NULL,NULL),(161,1,6,4,3,NULL,'2026-12-16 15:00:00',60,'Agendado',NULL,NULL),(162,46,9,1,2,NULL,'2026-12-15 15:00:00',30,'Agendado',NULL,NULL),(163,44,6,4,1,NULL,'2026-12-12 15:00:00',60,'Agendado',NULL,NULL),(164,42,9,1,3,NULL,'2026-12-11 15:00:00',30,'Agendado',NULL,NULL),(165,39,6,4,2,NULL,'2026-12-10 15:00:00',60,'Agendado',NULL,NULL),(166,38,9,1,1,NULL,'2026-12-09 15:00:00',30,'Agendado',NULL,NULL),(167,36,6,4,3,NULL,'2026-12-08 15:00:00',60,'Agendado',NULL,NULL),(168,33,9,1,2,NULL,'2026-12-05 15:00:00',30,'Agendado',NULL,NULL),(169,30,6,4,1,NULL,'2026-12-04 15:00:00',60,'Agendado',NULL,NULL),(170,28,9,1,3,NULL,'2026-12-03 15:00:00',30,'Agendado',NULL,NULL),(171,26,6,4,2,NULL,'2026-12-02 15:00:00',60,'Agendado',NULL,NULL),(172,24,9,1,1,NULL,'2026-12-01 15:00:00',30,'Agendado',NULL,NULL),(173,23,6,4,3,NULL,'2026-11-28 15:00:00',60,'Agendado',NULL,NULL),(174,20,9,1,2,NULL,'2026-11-27 15:00:00',30,'Agendado',NULL,NULL),(175,18,6,4,1,NULL,'2026-11-26 15:00:00',60,'Agendado',NULL,NULL),(176,15,9,1,3,NULL,'2026-11-25 15:00:00',30,'Agendado',NULL,NULL),(177,14,6,4,2,NULL,'2026-11-24 15:00:00',60,'Agendado',NULL,NULL),(178,12,9,1,1,NULL,'2026-11-21 15:00:00',30,'Agendado',NULL,NULL),(179,9,6,4,3,NULL,'2026-11-20 15:00:00',60,'Agendado',NULL,NULL),(180,7,9,1,2,NULL,'2026-11-19 15:00:00',30,'Agendado',NULL,NULL),(181,3,6,4,1,NULL,'2026-11-18 15:00:00',60,'Agendado',NULL,NULL),(182,1,9,1,3,NULL,'2026-11-17 15:00:00',30,'Agendado',NULL,NULL),(183,46,6,4,2,NULL,'2026-11-14 15:00:00',60,'Agendado',NULL,NULL),(184,44,9,1,1,NULL,'2026-11-13 15:00:00',30,'Agendado',NULL,NULL),(185,42,6,4,3,NULL,'2026-11-12 15:00:00',60,'Agendado',NULL,NULL),(186,39,9,1,2,NULL,'2026-11-11 15:00:00',30,'Agendado',NULL,NULL),(187,38,6,4,1,NULL,'2026-11-10 15:00:00',60,'Agendado',NULL,NULL),(188,36,9,1,3,NULL,'2026-11-07 15:00:00',30,'Agendado',NULL,NULL),(189,33,6,4,2,NULL,'2026-11-06 15:00:00',60,'Agendado',NULL,NULL),(190,30,9,1,1,NULL,'2026-11-05 15:00:00',30,'Agendado',NULL,NULL),(191,28,6,4,3,NULL,'2026-11-04 15:00:00',60,'Agendado',NULL,NULL),(192,26,9,1,2,NULL,'2026-11-03 15:00:00',30,'Agendado',NULL,NULL),(193,24,6,4,1,NULL,'2026-10-31 15:00:00',60,'Agendado',NULL,NULL),(194,23,9,1,3,NULL,'2026-10-30 15:00:00',30,'Agendado',NULL,NULL),(195,20,6,4,2,NULL,'2026-10-29 15:00:00',60,'Agendado',NULL,NULL),(196,18,9,1,1,NULL,'2026-10-28 15:00:00',30,'Agendado',NULL,NULL),(197,15,6,4,3,NULL,'2026-10-27 15:00:00',60,'Agendado',NULL,NULL),(198,14,9,1,2,NULL,'2026-10-24 15:00:00',30,'Agendado',NULL,NULL),(199,12,6,4,1,NULL,'2026-10-23 15:00:00',60,'Agendado',NULL,NULL),(200,9,9,1,3,NULL,'2026-10-22 15:00:00',30,'Agendado',NULL,NULL),(201,7,6,4,2,NULL,'2026-10-21 15:00:00',60,'Realizado',NULL,NULL),(202,3,9,1,1,NULL,'2026-10-20 15:00:00',30,'Agendado',NULL,NULL),(203,4,7,1,2,NULL,'2026-12-22 17:00:00',50,'Agendado',NULL,NULL),(204,2,10,5,1,NULL,'2026-12-19 17:00:00',40,'Agendado',NULL,NULL),(205,47,7,1,3,NULL,'2026-12-18 17:00:00',50,'Agendado',NULL,NULL),(206,43,10,5,2,NULL,'2026-12-17 17:00:00',40,'Agendado',NULL,NULL),(207,40,7,1,1,NULL,'2026-12-16 17:00:00',50,'Agendado',NULL,NULL),(208,35,10,5,3,NULL,'2026-12-15 17:00:00',40,'Agendado',NULL,NULL),(209,31,7,1,2,NULL,'2026-12-12 17:00:00',50,'Agendado',NULL,NULL),(210,27,10,5,1,NULL,'2026-12-11 17:00:00',40,'Agendado',NULL,NULL),(211,21,7,1,3,NULL,'2026-12-10 17:00:00',50,'Agendado',NULL,NULL),(212,17,10,5,2,NULL,'2026-12-09 17:00:00',40,'Agendado',NULL,NULL),(213,13,7,1,1,NULL,'2026-12-08 17:00:00',50,'Agendado',NULL,NULL),(214,11,10,5,3,NULL,'2026-12-05 17:00:00',40,'Agendado',NULL,NULL),(215,10,7,1,2,NULL,'2026-12-04 17:00:00',50,'Agendado',NULL,NULL),(216,8,10,5,1,NULL,'2026-12-03 17:00:00',40,'Agendado',NULL,NULL),(217,6,7,1,3,NULL,'2026-12-02 17:00:00',50,'Agendado',NULL,NULL),(218,4,10,5,2,NULL,'2026-12-01 17:00:00',40,'Agendado',NULL,NULL),(219,2,7,1,1,NULL,'2026-11-28 17:00:00',50,'Agendado',NULL,NULL),(220,47,10,5,3,NULL,'2026-11-27 17:00:00',40,'Agendado',NULL,NULL),(221,43,7,1,2,NULL,'2026-11-26 17:00:00',50,'Agendado',NULL,NULL),(222,40,10,5,1,NULL,'2026-11-25 17:00:00',40,'Agendado',NULL,NULL),(223,35,7,1,3,NULL,'2026-11-24 17:00:00',50,'Agendado',NULL,NULL),(224,31,10,5,2,NULL,'2026-11-21 17:00:00',40,'Agendado',NULL,NULL),(225,27,7,1,1,NULL,'2026-11-20 17:00:00',50,'Agendado',NULL,NULL),(226,21,10,5,3,NULL,'2026-11-19 17:00:00',40,'Agendado',NULL,NULL),(227,17,7,1,2,NULL,'2026-11-18 17:00:00',50,'Agendado',NULL,NULL),(228,13,10,5,1,NULL,'2026-11-17 17:00:00',40,'Agendado',NULL,NULL),(229,11,7,1,3,NULL,'2026-11-14 17:00:00',50,'Agendado',NULL,NULL),(230,10,10,5,2,NULL,'2026-11-13 17:00:00',40,'Agendado',NULL,NULL),(231,8,7,1,1,NULL,'2026-11-12 17:00:00',50,'Agendado',NULL,NULL),(232,6,10,5,3,NULL,'2026-11-11 17:00:00',40,'Agendado',NULL,NULL),(233,4,7,1,2,NULL,'2026-11-10 17:00:00',50,'Agendado',NULL,NULL),(234,2,10,5,1,NULL,'2026-11-07 17:00:00',40,'Agendado',NULL,NULL),(235,47,7,1,3,NULL,'2026-11-06 17:00:00',50,'Agendado',NULL,NULL),(236,43,10,5,2,NULL,'2026-11-05 17:00:00',40,'Agendado',NULL,NULL),(237,40,7,1,1,NULL,'2026-11-04 17:00:00',50,'Agendado',NULL,NULL),(238,35,10,5,3,NULL,'2026-11-03 17:00:00',40,'Agendado',NULL,NULL),(239,31,7,1,2,NULL,'2026-10-31 17:00:00',50,'Agendado',NULL,NULL),(240,27,10,5,1,NULL,'2026-10-30 17:00:00',40,'Agendado',NULL,NULL),(241,21,7,1,3,NULL,'2026-10-29 17:00:00',50,'Agendado',NULL,NULL),(242,17,10,5,2,NULL,'2026-10-28 17:00:00',40,'Agendado',NULL,NULL),(243,13,7,1,1,NULL,'2026-10-27 17:00:00',50,'Agendado',NULL,NULL),(244,11,10,5,3,NULL,'2026-10-24 17:00:00',40,'Agendado',NULL,NULL),(245,10,7,1,2,NULL,'2026-10-23 17:00:00',50,'Agendado',NULL,NULL),(246,8,10,5,1,NULL,'2026-10-22 17:00:00',40,'Agendado',NULL,NULL),(247,6,7,1,3,NULL,'2026-10-21 17:00:00',50,'Realizado',NULL,NULL),(248,4,10,5,2,NULL,'2026-10-20 17:00:00',40,'Agendado',NULL,NULL),(274,20,1,1,3,1,'2026-10-17 18:00:00',60,'Realizado',NULL,NULL),(275,6,3,1,2,4,'2026-10-17 16:00:00',45,'Realizado',NULL,NULL),(276,6,5,5,1,3,'2026-10-20 10:00:00',30,'Faltou',NULL,NULL),(277,47,9,3,2,NULL,'2026-10-24 18:00:00',30,'Agendado',NULL,NULL);


-- Dump completed on 2026-10-25 15:08:34


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `venda`
--

DROP TABLE IF EXISTS `venda`;
CREATE TABLE `venda` (
  `id_venda` int NOT NULL AUTO_INCREMENT,
  `id_paciente` int NOT NULL,
  `id_pacote` int DEFAULT NULL,
  `id_procedimento` int DEFAULT NULL,
  `valor_total` decimal(10,2) NOT NULL,
  `data_venda` date NOT NULL,
  `forma_pagamento` varchar(50) NOT NULL,
  `valor_entrada` decimal(10,2) DEFAULT NULL,
  PRIMARY KEY (`id_venda`),
  CONSTRAINT `fk_venda_paciente` FOREIGN KEY (`id_paciente`) REFERENCES `paciente` (`id_paciente`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_venda_pacote` FOREIGN KEY (`id_pacote`) REFERENCES `pacote` (`id_pacote`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_venda_procedimento` FOREIGN KEY (`id_procedimento`) REFERENCES `procedimento` (`id_procedimento`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `chk_venda_item_unico` CHECK ((((`id_pacote` is not null) and (`id_procedimento` is null)) or ((`id_pacote` is null) and (`id_procedimento` is not null))))
);

--
-- Dumping data for table `venda`
--

INSERT INTO `venda` VALUES (1,2,1,NULL,3400.00,'2026-08-01','Crédito',400.00),(2,6,2,NULL,3700.00,'2026-08-20','Crédito',NULL),(3,7,3,NULL,2450.00,'2026-08-15','Crédito',450.00),(4,4,4,NULL,2850.00,'2026-07-10','Crédito',NULL),(5,10,5,NULL,1950.00,'2026-08-25','Crédito',150.00),(6,1,NULL,3,210.00,'2026-08-10','Pix',NULL),(7,8,NULL,7,140.00,'2026-08-28','Débito',NULL),(8,3,NULL,5,150.00,'2026-08-29','Débito',NULL),(9,5,NULL,2,195.00,'2026-07-15','Pix',NULL),(10,9,NULL,8,180.00,'2026-08-30','Pix',NULL),(11,20,2,NULL,3700.00,'2026-10-17','Crédito',0.00),(12,6,4,NULL,2850.00,'2026-10-17','Crédito',0.00),(13,16,NULL,6,160.00,'2026-12-22','Débito',NULL),(14,5,NULL,6,160.00,'2026-12-19','Crédito',NULL),(15,45,NULL,6,160.00,'2026-12-18','Pix',NULL),(16,41,NULL,6,160.00,'2026-12-17','Débito',NULL),(17,37,NULL,6,160.00,'2026-12-16','Crédito',NULL),(18,34,NULL,6,160.00,'2026-12-15','Pix',NULL),(19,32,NULL,6,160.00,'2026-12-12','Débito',NULL),(20,29,NULL,6,160.00,'2026-12-11','Crédito',NULL),(21,25,NULL,6,160.00,'2026-12-10','Pix',NULL),(22,22,NULL,6,160.00,'2026-12-09','Débito',NULL),(23,19,NULL,6,160.00,'2026-12-08','Crédito',NULL),(24,16,NULL,6,160.00,'2026-12-05','Pix',NULL),(25,5,NULL,6,160.00,'2026-12-04','Débito',NULL),(26,45,NULL,6,160.00,'2026-12-03','Crédito',NULL),(27,41,NULL,6,160.00,'2026-12-02','Pix',NULL),(28,37,NULL,6,160.00,'2026-12-01','Débito',NULL),(29,34,NULL,6,160.00,'2026-11-28','Crédito',NULL),(30,32,NULL,6,160.00,'2026-11-27','Pix',NULL),(31,29,NULL,6,160.00,'2026-11-26','Débito',NULL),(32,25,NULL,6,160.00,'2026-11-25','Crédito',NULL),(33,22,NULL,6,160.00,'2026-11-24','Pix',NULL),(34,19,NULL,6,160.00,'2026-11-21','Débito',NULL),(35,16,NULL,6,160.00,'2026-11-20','Crédito',NULL),(36,5,NULL,6,160.00,'2026-11-19','Pix',NULL),(37,45,NULL,6,160.00,'2026-11-18','Débito',NULL),(38,41,NULL,6,160.00,'2026-11-17','Crédito',NULL),(39,37,NULL,6,160.00,'2026-11-14','Pix',NULL),(40,34,NULL,6,160.00,'2026-11-13','Débito',NULL),(41,32,NULL,6,160.00,'2026-11-12','Crédito',NULL),(42,29,NULL,6,160.00,'2026-11-11','Pix',NULL),(43,25,NULL,6,160.00,'2026-11-10','Débito',NULL),(44,22,NULL,6,160.00,'2026-11-07','Crédito',NULL),(45,19,NULL,6,160.00,'2026-11-06','Pix',NULL),(46,16,NULL,6,160.00,'2026-11-05','Débito',NULL),(47,5,NULL,6,160.00,'2026-11-04','Crédito',NULL),(48,45,NULL,6,160.00,'2026-11-03','Pix',NULL),(49,41,NULL,6,160.00,'2026-10-31','Débito',NULL),(50,37,NULL,6,160.00,'2026-10-30','Crédito',NULL),(51,34,NULL,6,160.00,'2026-10-29','Pix',NULL),(52,32,NULL,6,160.00,'2026-10-28','Débito',NULL),(53,29,NULL,6,160.00,'2026-10-27','Crédito',NULL),(54,25,NULL,6,160.00,'2026-10-24','Pix',NULL),(55,22,NULL,6,160.00,'2026-10-23','Débito',NULL),(56,19,NULL,6,160.00,'2026-10-22','Crédito',NULL),(57,16,NULL,6,160.00,'2026-10-21','Pix',NULL),(58,5,NULL,6,160.00,'2026-10-20','Débito',NULL),(59,12,NULL,6,160.00,'2026-12-22','Débito',NULL),(60,7,NULL,6,160.00,'2026-12-18','Pix',NULL),(61,1,NULL,6,160.00,'2026-12-16','Crédito',NULL),(62,44,NULL,6,160.00,'2026-12-12','Débito',NULL),(63,39,NULL,6,160.00,'2026-12-10','Pix',NULL),(64,36,NULL,6,160.00,'2026-12-08','Crédito',NULL),(65,30,NULL,6,160.00,'2026-12-04','Débito',NULL),(66,26,NULL,6,160.00,'2026-12-02','Pix',NULL),(67,23,NULL,6,160.00,'2026-11-28','Crédito',NULL),(68,18,NULL,6,160.00,'2026-11-26','Débito',NULL),(69,14,NULL,6,160.00,'2026-11-24','Pix',NULL),(70,9,NULL,6,160.00,'2026-11-20','Crédito',NULL),(71,3,NULL,6,160.00,'2026-11-18','Débito',NULL),(72,46,NULL,6,160.00,'2026-11-14','Pix',NULL),(73,42,NULL,6,160.00,'2026-11-12','Crédito',NULL),(74,38,NULL,6,160.00,'2026-11-10','Débito',NULL),(75,33,NULL,6,160.00,'2026-11-06','Pix',NULL),(76,28,NULL,6,160.00,'2026-11-04','Crédito',NULL),(77,24,NULL,6,160.00,'2026-10-31','Débito',NULL),(78,20,NULL,6,160.00,'2026-10-29','Pix',NULL),(79,15,NULL,6,160.00,'2026-10-27','Crédito',NULL),(80,12,NULL,6,160.00,'2026-10-23','Débito',NULL),(81,7,NULL,6,160.00,'2026-10-21','Pix',NULL),(82,9,NULL,7,140.00,'2026-12-22','Crédito',NULL),(83,7,NULL,7,140.00,'2026-12-19','Pix',NULL),(84,3,NULL,7,140.00,'2026-12-18','Débito',NULL),(85,1,NULL,7,140.00,'2026-12-17','Crédito',NULL),(86,46,NULL,7,140.00,'2026-12-16','Pix',NULL),(87,44,NULL,7,140.00,'2026-12-15','Débito',NULL),(88,42,NULL,7,140.00,'2026-12-12','Crédito',NULL),(89,39,NULL,7,140.00,'2026-12-11','Pix',NULL),(90,38,NULL,7,140.00,'2026-12-10','Débito',NULL),(91,36,NULL,7,140.00,'2026-12-09','Crédito',NULL),(92,33,NULL,7,140.00,'2026-12-08','Pix',NULL),(93,30,NULL,7,140.00,'2026-12-05','Débito',NULL),(94,28,NULL,7,140.00,'2026-12-04','Crédito',NULL),(95,26,NULL,7,140.00,'2026-12-03','Pix',NULL),(96,24,NULL,7,140.00,'2026-12-02','Débito',NULL),(97,23,NULL,7,140.00,'2026-12-01','Crédito',NULL),(98,20,NULL,7,140.00,'2026-11-28','Pix',NULL),(99,18,NULL,7,140.00,'2026-11-27','Débito',NULL),(100,15,NULL,7,140.00,'2026-11-26','Crédito',NULL),(101,14,NULL,7,140.00,'2026-11-25','Pix',NULL),(102,12,NULL,7,140.00,'2026-11-24','Débito',NULL),(103,9,NULL,7,140.00,'2026-11-21','Crédito',NULL),(104,7,NULL,7,140.00,'2026-11-20','Pix',NULL),(105,3,NULL,7,140.00,'2026-11-19','Débito',NULL),(106,1,NULL,7,140.00,'2026-11-18','Crédito',NULL),(107,46,NULL,7,140.00,'2026-11-17','Pix',NULL),(108,44,NULL,7,140.00,'2026-11-14','Débito',NULL),(109,42,NULL,7,140.00,'2026-11-13','Crédito',NULL),(110,39,NULL,7,140.00,'2026-11-12','Pix',NULL),(111,38,NULL,7,140.00,'2026-11-11','Débito',NULL),(112,36,NULL,7,140.00,'2026-11-10','Crédito',NULL),(113,33,NULL,7,140.00,'2026-11-07','Pix',NULL),(114,30,NULL,7,140.00,'2026-11-06','Débito',NULL),(115,28,NULL,7,140.00,'2026-11-05','Crédito',NULL),(116,26,NULL,7,140.00,'2026-11-04','Pix',NULL),(117,24,NULL,7,140.00,'2026-11-03','Débito',NULL),(118,23,NULL,7,140.00,'2026-10-31','Crédito',NULL),(119,20,NULL,7,140.00,'2026-10-30','Pix',NULL),(120,18,NULL,7,140.00,'2026-10-29','Débito',NULL),(121,15,NULL,7,140.00,'2026-10-28','Crédito',NULL),(122,14,NULL,7,140.00,'2026-10-27','Pix',NULL),(123,12,NULL,7,140.00,'2026-10-24','Débito',NULL),(124,9,NULL,7,140.00,'2026-10-23','Crédito',NULL),(125,7,NULL,7,140.00,'2026-10-22','Pix',NULL),(126,3,NULL,7,140.00,'2026-10-21','Débito',NULL),(127,1,NULL,7,140.00,'2026-10-20','Crédito',NULL),(128,4,NULL,7,140.00,'2026-12-22','Crédito',NULL),(129,47,NULL,7,140.00,'2026-12-18','Débito',NULL),(130,40,NULL,7,140.00,'2026-12-16','Pix',NULL),(131,31,NULL,7,140.00,'2026-12-12','Crédito',NULL),(132,21,NULL,7,140.00,'2026-12-10','Débito',NULL),(133,13,NULL,7,140.00,'2026-12-08','Pix',NULL),(134,10,NULL,7,140.00,'2026-12-04','Crédito',NULL),(135,6,NULL,7,140.00,'2026-12-02','Débito',NULL),(136,2,NULL,7,140.00,'2026-11-28','Pix',NULL),(137,43,NULL,7,140.00,'2026-11-26','Crédito',NULL),(138,35,NULL,7,140.00,'2026-11-24','Débito',NULL),(139,27,NULL,7,140.00,'2026-11-20','Pix',NULL),(140,17,NULL,7,140.00,'2026-11-18','Crédito',NULL),(141,11,NULL,7,140.00,'2026-11-14','Débito',NULL),(142,8,NULL,7,140.00,'2026-11-12','Pix',NULL),(143,4,NULL,7,140.00,'2026-11-10','Crédito',NULL),(144,47,NULL,7,140.00,'2026-11-06','Débito',NULL),(145,40,NULL,7,140.00,'2026-11-04','Pix',NULL),(146,31,NULL,7,140.00,'2026-10-31','Crédito',NULL),(147,21,NULL,7,140.00,'2026-10-29','Débito',NULL),(148,13,NULL,7,140.00,'2026-10-27','Pix',NULL),(149,10,NULL,7,140.00,'2026-10-23','Crédito',NULL),(150,6,NULL,7,140.00,'2026-10-21','Débito',NULL),(151,47,NULL,8,180.00,'2026-12-19','Débito',NULL),(152,40,NULL,8,180.00,'2026-12-17','Pix',NULL),(153,31,NULL,8,180.00,'2026-12-15','Crédito',NULL),(154,21,NULL,8,180.00,'2026-12-11','Débito',NULL),(155,13,NULL,8,180.00,'2026-12-09','Pix',NULL),(156,10,NULL,8,180.00,'2026-12-05','Crédito',NULL),(157,6,NULL,8,180.00,'2026-12-03','Débito',NULL),(158,2,NULL,8,180.00,'2026-12-01','Pix',NULL),(159,43,NULL,8,180.00,'2026-11-27','Crédito',NULL),(160,35,NULL,8,180.00,'2026-11-25','Débito',NULL),(161,27,NULL,8,180.00,'2026-11-21','Pix',NULL),(162,17,NULL,8,180.00,'2026-11-19','Crédito',NULL),(163,11,NULL,8,180.00,'2026-11-17','Débito',NULL),(164,8,NULL,8,180.00,'2026-11-13','Pix',NULL),(165,4,NULL,8,180.00,'2026-11-11','Crédito',NULL),(166,47,NULL,8,180.00,'2026-11-07','Débito',NULL),(167,40,NULL,8,180.00,'2026-11-05','Pix',NULL),(168,31,NULL,8,180.00,'2026-11-03','Crédito',NULL),(169,21,NULL,8,180.00,'2026-10-30','Débito',NULL),(170,13,NULL,8,180.00,'2026-10-28','Pix',NULL),(171,10,NULL,8,180.00,'2026-10-24','Crédito',NULL),(172,6,NULL,8,180.00,'2026-10-22','Débito',NULL),(173,2,NULL,8,180.00,'2026-10-20','Pix',NULL),(174,2,NULL,9,80.00,'2026-12-22','Pix',NULL),(175,43,NULL,9,80.00,'2026-12-18','Crédito',NULL),(176,35,NULL,9,80.00,'2026-12-16','Débito',NULL),(177,27,NULL,9,80.00,'2026-12-12','Pix',NULL),(178,17,NULL,9,80.00,'2026-12-10','Crédito',NULL),(179,11,NULL,9,80.00,'2026-12-08','Débito',NULL),(180,8,NULL,9,80.00,'2026-12-04','Pix',NULL),(181,4,NULL,9,80.00,'2026-12-02','Crédito',NULL),(182,47,NULL,9,80.00,'2026-11-28','Débito',NULL),(183,40,NULL,9,80.00,'2026-11-26','Pix',NULL),(184,31,NULL,9,80.00,'2026-11-24','Crédito',NULL),(185,21,NULL,9,80.00,'2026-11-20','Débito',NULL),(186,13,NULL,9,80.00,'2026-11-18','Pix',NULL),(187,10,NULL,9,80.00,'2026-11-14','Crédito',NULL),(188,6,NULL,9,80.00,'2026-11-12','Débito',NULL),(189,2,NULL,9,80.00,'2026-11-10','Pix',NULL),(190,43,NULL,9,80.00,'2026-11-06','Crédito',NULL),(191,35,NULL,9,80.00,'2026-11-04','Débito',NULL),(192,27,NULL,9,80.00,'2026-10-31','Pix',NULL),(193,17,NULL,9,80.00,'2026-10-29','Crédito',NULL),(194,11,NULL,9,80.00,'2026-10-27','Débito',NULL),(195,8,NULL,9,80.00,'2026-10-23','Pix',NULL),(196,4,NULL,9,80.00,'2026-10-21','Crédito',NULL),(197,9,NULL,9,80.00,'2026-12-19','Crédito',NULL),(198,3,NULL,9,80.00,'2026-12-17','Débito',NULL),(199,46,NULL,9,80.00,'2026-12-15','Pix',NULL),(200,42,NULL,9,80.00,'2026-12-11','Crédito',NULL),(201,38,NULL,9,80.00,'2026-12-09','Débito',NULL),(202,33,NULL,9,80.00,'2026-12-05','Pix',NULL),(203,28,NULL,9,80.00,'2026-12-03','Crédito',NULL),(204,24,NULL,9,80.00,'2026-12-01','Débito',NULL),(205,20,NULL,9,80.00,'2026-11-27','Pix',NULL),(206,15,NULL,9,80.00,'2026-11-25','Crédito',NULL),(207,12,NULL,9,80.00,'2026-11-21','Débito',NULL),(208,7,NULL,9,80.00,'2026-11-19','Pix',NULL),(209,1,NULL,9,80.00,'2026-11-17','Crédito',NULL),(210,44,NULL,9,80.00,'2026-11-13','Débito',NULL),(211,39,NULL,9,80.00,'2026-11-11','Pix',NULL),(212,36,NULL,9,80.00,'2026-11-07','Crédito',NULL),(213,30,NULL,9,80.00,'2026-11-05','Débito',NULL),(214,26,NULL,9,80.00,'2026-11-03','Pix',NULL),(215,23,NULL,9,80.00,'2026-10-30','Crédito',NULL),(216,18,NULL,9,80.00,'2026-10-28','Débito',NULL),(217,14,NULL,9,80.00,'2026-10-24','Pix',NULL),(218,9,NULL,9,80.00,'2026-10-22','Crédito',NULL),(219,3,NULL,9,80.00,'2026-10-20','Débito',NULL),(220,2,NULL,10,250.00,'2026-12-19','Pix',NULL),(221,43,NULL,10,250.00,'2026-12-17','Crédito',NULL),(222,35,NULL,10,250.00,'2026-12-15','Débito',NULL),(223,27,NULL,10,250.00,'2026-12-11','Pix',NULL),(224,17,NULL,10,250.00,'2026-12-09','Crédito',NULL),(225,11,NULL,10,250.00,'2026-12-05','Débito',NULL),(226,8,NULL,10,250.00,'2026-12-03','Pix',NULL),(227,4,NULL,10,250.00,'2026-12-01','Crédito',NULL),(228,47,NULL,10,250.00,'2026-11-27','Débito',NULL),(229,40,NULL,10,250.00,'2026-11-25','Pix',NULL),(230,31,NULL,10,250.00,'2026-11-21','Crédito',NULL),(231,21,NULL,10,250.00,'2026-11-19','Débito',NULL),(232,13,NULL,10,250.00,'2026-11-17','Pix',NULL),(233,10,NULL,10,250.00,'2026-11-13','Crédito',NULL),(234,6,NULL,10,250.00,'2026-11-11','Débito',NULL),(235,2,NULL,10,250.00,'2026-11-07','Pix',NULL),(236,43,NULL,10,250.00,'2026-11-05','Crédito',NULL),(237,35,NULL,10,250.00,'2026-11-03','Débito',NULL),(238,27,NULL,10,250.00,'2026-10-30','Pix',NULL),(239,17,NULL,10,250.00,'2026-10-28','Crédito',NULL),(240,11,NULL,10,250.00,'2026-10-24','Débito',NULL),(241,8,NULL,10,250.00,'2026-10-22','Pix',NULL),(242,4,NULL,10,250.00,'2026-10-20','Crédito',NULL),(268,40,NULL,10,250.00,'2026-10-21','Pix',250.00),(269,34,NULL,10,250.00,'2026-10-21','Pix',250.00),(270,34,NULL,10,250.00,'2026-10-21','Pix',250.00),(271,34,NULL,10,250.00,'2026-10-21','Pix',250.00),(272,34,NULL,10,250.00,'2026-10-21','Pix',250.00),(273,34,NULL,10,250.00,'2026-10-21','Pix',250.00),(274,34,NULL,10,250.00,'2026-10-21','Pix',250.00),(275,34,NULL,10,250.00,'2026-10-21','Pix',250.00),(276,34,NULL,10,250.00,'2026-10-21','Pix',250.00),(277,34,NULL,10,250.00,'2026-10-21','Pix',250.00),(278,34,NULL,10,250.00,'2026-10-21','Pix',250.00),(279,34,NULL,10,250.00,'2026-10-21','Pix',250.00),(280,34,NULL,10,250.00,'2026-10-21','Pix',250.00),(281,34,NULL,10,250.00,'2026-10-21','Pix',250.00),(282,34,NULL,10,250.00,'2026-10-21','Pix',250.00),(283,34,NULL,10,250.00,'2026-10-21','Pix',250.00),(284,41,NULL,1,240.00,'2026-10-21','Crédito',240.00),(285,41,2,NULL,3700.00,'2026-10-21','Crédito',0.00),(286,41,2,NULL,3700.00,'2026-10-21','Crédito',0.00),(287,41,2,NULL,3700.00,'2026-10-21','Crédito',0.00),(288,41,2,NULL,3700.00,'2026-10-21','Crédito',0.00),(289,41,2,NULL,3700.00,'2026-10-21','Crédito',0.00),(290,19,NULL,10,250.00,'2026-10-21','Crédito',250.00);


-- Dump completed on 2026-10-25 15:08:34


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `parcela`
--

DROP TABLE IF EXISTS `parcela`;
CREATE TABLE `parcela` (
  `id_parcela` int NOT NULL AUTO_INCREMENT,
  `id_venda` int NOT NULL,
  `numero_parcela` int NOT NULL,
  `valor_parcela` decimal(10,2) NOT NULL,
  `data_vencimento` date NOT NULL,
  `data_pagamento` date DEFAULT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'Pendente',
  PRIMARY KEY (`id_parcela`),
  UNIQUE KEY `uq_parcela_venda_numero` (`id_venda`,`numero_parcela`),
  CONSTRAINT `fk_parcela_venda` FOREIGN KEY (`id_venda`) REFERENCES `venda` (`id_venda`) ON DELETE RESTRICT ON UPDATE CASCADE
);

--
-- Dumping data for table `parcela`
--

INSERT INTO `parcela` VALUES (1,1,1,400.00,'2026-08-01','2026-08-01','Paga'),(2,1,2,600.00,'2026-09-01',NULL,'Vencida'),(3,1,3,600.00,'2026-10-01',NULL,'Vencida'),(4,1,4,600.00,'2026-11-01',NULL,'Pendente'),(5,1,5,600.00,'2026-12-01',NULL,'Pendente'),(6,1,6,600.00,'2027-01-01',NULL,'Pendente'),(7,2,1,740.00,'2026-08-20','2026-08-20','Paga'),(8,2,2,740.00,'2026-09-20',NULL,'Vencida'),(9,2,3,740.00,'2026-10-20',NULL,'Vencida'),(10,2,4,740.00,'2026-11-20',NULL,'Pendente'),(11,2,5,740.00,'2026-12-20',NULL,'Pendente'),(12,3,1,450.00,'2026-08-15','2026-08-15','Paga'),(13,3,2,500.00,'2026-09-15',NULL,'Vencida'),(14,3,3,500.00,'2026-10-15',NULL,'Vencida'),(15,3,4,500.00,'2026-11-15',NULL,'Pendente'),(16,3,5,500.00,'2026-12-15',NULL,'Pendente'),(17,4,1,712.50,'2026-07-10','2026-07-10','Paga'),(18,4,2,712.50,'2026-08-10',NULL,'Vencida'),(19,4,3,712.50,'2026-09-10',NULL,'Vencida'),(20,4,4,712.50,'2026-10-10',NULL,'Vencida'),(21,5,1,150.00,'2026-08-25','2026-08-25','Paga'),(22,5,2,600.00,'2026-09-25',NULL,'Vencida'),(23,5,3,600.00,'2026-10-25',NULL,'Pendente'),(24,5,4,600.00,'2026-11-25',NULL,'Pendente'),(25,6,1,210.00,'2026-08-10','2026-08-10','Paga'),(26,7,1,140.00,'2026-08-28','2026-08-28','Paga'),(27,8,1,150.00,'2026-08-29','2026-08-29','Paga'),(28,9,1,195.00,'2026-07-15','2026-07-15','Paga'),(29,10,1,180.00,'2026-08-30','2026-08-30','Paga'),(30,11,1,370.00,'2026-11-17',NULL,'Pendente'),(31,11,2,370.00,'2026-12-17',NULL,'Pendente'),(32,11,3,370.00,'2027-01-17',NULL,'Pendente'),(33,11,4,370.00,'2027-02-17',NULL,'Pendente'),(34,11,5,370.00,'2027-03-17',NULL,'Pendente'),(35,11,6,370.00,'2027-04-17',NULL,'Pendente'),(36,11,7,370.00,'2027-05-17',NULL,'Pendente'),(37,11,8,370.00,'2027-06-17',NULL,'Pendente'),(38,11,9,370.00,'2027-07-17',NULL,'Pendente'),(39,11,10,370.00,'2027-08-17',NULL,'Pendente'),(40,12,1,950.00,'2026-11-17',NULL,'Pendente'),(41,12,2,950.00,'2026-12-17',NULL,'Pendente'),(42,12,3,950.00,'2027-01-17',NULL,'Pendente'),(43,285,1,370.00,'2026-11-21',NULL,'Pendente'),(44,285,2,370.00,'2026-12-21',NULL,'Pendente'),(45,285,3,370.00,'2027-01-21',NULL,'Pendente'),(46,285,4,370.00,'2027-02-21',NULL,'Pendente'),(47,285,5,370.00,'2027-03-21',NULL,'Pendente'),(48,285,6,370.00,'2027-04-21',NULL,'Pendente'),(49,285,7,370.00,'2027-05-21',NULL,'Pendente'),(50,285,8,370.00,'2027-06-21',NULL,'Pendente'),(51,285,9,370.00,'2027-07-21',NULL,'Pendente'),(52,285,10,370.00,'2027-08-21',NULL,'Pendente'),(53,286,1,370.00,'2026-11-21',NULL,'Pendente'),(54,286,2,370.00,'2026-12-21',NULL,'Pendente'),(55,286,3,370.00,'2027-01-21',NULL,'Pendente'),(56,286,4,370.00,'2027-02-21',NULL,'Pendente'),(57,286,5,370.00,'2027-03-21',NULL,'Pendente'),(58,286,6,370.00,'2027-04-21',NULL,'Pendente'),(59,286,7,370.00,'2027-05-21',NULL,'Pendente'),(60,286,8,370.00,'2027-06-21',NULL,'Pendente'),(61,286,9,370.00,'2027-07-21',NULL,'Pendente'),(62,286,10,370.00,'2027-08-21',NULL,'Pendente'),(63,287,1,370.00,'2026-11-21',NULL,'Pendente'),(64,287,2,370.00,'2026-12-21',NULL,'Pendente'),(65,287,3,370.00,'2027-01-21',NULL,'Pendente'),(66,287,4,370.00,'2027-02-21',NULL,'Pendente'),(67,287,5,370.00,'2027-03-21',NULL,'Pendente'),(68,287,6,370.00,'2027-04-21',NULL,'Pendente'),(69,287,7,370.00,'2027-05-21',NULL,'Pendente'),(70,287,8,370.00,'2027-06-21',NULL,'Pendente'),(71,287,9,370.00,'2027-07-21',NULL,'Pendente'),(72,287,10,370.00,'2027-08-21',NULL,'Pendente'),(73,288,1,370.00,'2026-11-21',NULL,'Pendente'),(74,288,2,370.00,'2026-12-21',NULL,'Pendente'),(75,288,3,370.00,'2027-01-21',NULL,'Pendente'),(76,288,4,370.00,'2027-02-21',NULL,'Pendente'),(77,288,5,370.00,'2027-03-21',NULL,'Pendente'),(78,288,6,370.00,'2027-04-21',NULL,'Pendente'),(79,288,7,370.00,'2027-05-21',NULL,'Pendente'),(80,288,8,370.00,'2027-06-21',NULL,'Pendente'),(81,288,9,370.00,'2027-07-21',NULL,'Pendente'),(82,288,10,370.00,'2027-08-21',NULL,'Pendente'),(83,289,1,370.00,'2026-11-21',NULL,'Pendente'),(84,289,2,370.00,'2026-12-21',NULL,'Pendente'),(85,289,3,370.00,'2027-01-21',NULL,'Pendente'),(86,289,4,370.00,'2027-02-21',NULL,'Pendente'),(87,289,5,370.00,'2027-03-21',NULL,'Pendente'),(88,289,6,370.00,'2027-04-21',NULL,'Pendente'),(89,289,7,370.00,'2027-05-21',NULL,'Pendente'),(90,289,8,370.00,'2027-06-21',NULL,'Pendente'),(91,289,9,370.00,'2027-07-21',NULL,'Pendente'),(92,289,10,370.00,'2027-08-21',NULL,'Pendente');


-- Dump completed on 2026-10-25 15:08:35


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `sessao_atendimento`
--

DROP TABLE IF EXISTS `sessao_atendimento`;
CREATE TABLE `sessao_atendimento` (
  `id_sessao` int NOT NULL AUTO_INCREMENT,
  `id_agendamento` int NOT NULL,
  `id_prontuario` int NOT NULL,
  `data` date NOT NULL,
  `id_procedimento` int NOT NULL,
  `id_profissional` int NOT NULL,
  `id_equipamento` int DEFAULT NULL,
  `regiao_tratada` varchar(150) DEFAULT NULL,
  `tecnica_utilizada` varchar(150) DEFAULT NULL,
  `numero_lote` varchar(50) DEFAULT NULL,
  `fabricante` varchar(150) DEFAULT NULL,
  `validade_lote` date DEFAULT NULL,
  `descricao_evolucao` text,
  `intercorrencias` text,
  PRIMARY KEY (`id_sessao`),
  UNIQUE KEY `uq_sessao_agendamento` (`id_agendamento`),
  CONSTRAINT `fk_sessao_agendamento` FOREIGN KEY (`id_agendamento`) REFERENCES `agendamento` (`id_agendamento`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_sessao_equipamento` FOREIGN KEY (`id_equipamento`) REFERENCES `equipamento` (`id_equipamento`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_sessao_procedimento` FOREIGN KEY (`id_procedimento`) REFERENCES `procedimento` (`id_procedimento`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_sessao_profissional` FOREIGN KEY (`id_profissional`) REFERENCES `profissional` (`id_profissional`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_sessao_prontuario` FOREIGN KEY (`id_prontuario`) REFERENCES `prontuario` (`id_prontuario`) ON DELETE RESTRICT ON UPDATE CASCADE
);

--
-- Dumping data for table `sessao_atendimento`
--

INSERT INTO `sessao_atendimento` VALUES (1,1,1,'2026-08-10',3,2,4,'Abdômen','Radiofrequência monopolar',NULL,NULL,NULL,'Boa resposta, sem intercorrência',NULL),(2,2,1,'2026-08-24',3,2,4,'Abdômen','Radiofrequência monopolar',NULL,NULL,NULL,'Leve vermelhidão, resolvida em horas',NULL),(3,6,5,'2026-07-15',2,3,2,'Axilas','Laser diodo',NULL,NULL,NULL,'Primeira sessão, pele reagiu bem',NULL),(4,10,9,'2026-10-17',8,4,NULL,NULL,NULL,NULL,NULL,NULL,'Foi realizado o peeling paciente não relatou desconforto e foi orientada aos cuidados a serem realizados em casa.',NULL),(5,7,6,'2026-10-17',4,3,5,NULL,NULL,NULL,NULL,NULL,'Foi realizado o procediemnto paciente não relatou desconforto durante a sessão.',NULL),(6,4,3,'2026-10-17',5,2,3,NULL,NULL,NULL,NULL,NULL,'realizado procediemnto, paciente relatou desconforto durante a sessão (ja esperado por se tratar de um procedimento invasivo)',NULL),(7,274,11,'2026-10-17',1,1,1,NULL,NULL,NULL,NULL,NULL,'realizado procedimento na barriga',NULL),(8,275,6,'2026-10-17',3,1,4,NULL,NULL,NULL,NULL,NULL,'realizado',NULL),(9,16,6,'2026-10-17',4,3,5,NULL,NULL,NULL,NULL,NULL,'realizado',NULL),(12,63,12,'2026-10-21',6,3,NULL,NULL,NULL,NULL,NULL,NULL,'realizado',NULL),(13,109,3,'2026-10-21',7,4,NULL,NULL,NULL,NULL,NULL,NULL,'realizado sem intercorrencias',NULL),(14,155,4,'2026-10-21',9,4,NULL,NULL,NULL,NULL,NULL,NULL,'realizado',NULL),(15,201,7,'2026-10-21',6,4,NULL,NULL,NULL,NULL,NULL,NULL,'realizado',NULL),(16,247,6,'2026-10-21',7,1,NULL,NULL,NULL,NULL,NULL,NULL,'realizado',NULL);


-- Dump completed on 2026-10-25 15:08:35


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `saldo_sessao`
--

DROP TABLE IF EXISTS `saldo_sessao`;
CREATE TABLE `saldo_sessao` (
  `id_saldo` int NOT NULL AUTO_INCREMENT,
  `id_paciente` int NOT NULL,
  `id_procedimento` int NOT NULL,
  `id_venda_origem` int NOT NULL,
  `quantidade_total` int NOT NULL,
  `quantidade_utilizada` int NOT NULL DEFAULT '0',
  `quantidade_disponivel` int GENERATED ALWAYS AS ((`quantidade_total` - `quantidade_utilizada`)),
  `data_ultima_sessao` date DEFAULT NULL,
  PRIMARY KEY (`id_saldo`),
  CONSTRAINT `fk_saldo_paciente` FOREIGN KEY (`id_paciente`) REFERENCES `paciente` (`id_paciente`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_saldo_procedimento` FOREIGN KEY (`id_procedimento`) REFERENCES `procedimento` (`id_procedimento`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_saldo_venda` FOREIGN KEY (`id_venda_origem`) REFERENCES `venda` (`id_venda`) ON DELETE RESTRICT ON UPDATE CASCADE
);

--
-- Dumping data for table `saldo_sessao`
--

INSERT INTO `saldo_sessao` (`id_saldo`, `id_paciente`, `id_procedimento`, `id_venda_origem`, `quantidade_total`, `quantidade_utilizada`, `data_ultima_sessao`) VALUES (1,2,1,1,10,0,NULL),(2,2,7,1,10,0,NULL),(3,6,4,2,10,1,'2026-10-17'),(4,6,1,2,8,0,NULL),(5,7,6,3,8,1,'2026-10-21'),(6,7,8,3,8,0,NULL),(7,4,5,4,10,0,NULL),(8,4,3,4,8,0,NULL),(9,10,2,5,10,0,NULL),(10,20,4,11,10,0,NULL),(11,20,1,11,8,1,'2026-10-17'),(12,6,5,12,10,0,NULL),(13,6,3,12,8,1,'2026-10-17'),(14,1,3,6,1,0,NULL),(15,8,7,7,1,0,NULL),(16,3,5,8,1,0,NULL),(17,5,2,9,1,0,NULL),(18,9,8,10,1,0,NULL),(19,16,6,13,1,1,'2026-10-21'),(20,5,6,14,1,0,NULL),(21,45,6,15,1,0,NULL),(22,41,6,16,1,0,NULL),(23,37,6,17,1,0,NULL),(24,34,6,18,1,0,NULL),(25,32,6,19,1,0,NULL),(26,29,6,20,1,0,NULL),(27,25,6,21,1,0,NULL),(28,22,6,22,1,0,NULL),(29,19,6,23,1,0,NULL),(30,16,6,24,1,0,NULL),(31,5,6,25,1,0,NULL),(32,45,6,26,1,0,NULL),(33,41,6,27,1,0,NULL),(34,37,6,28,1,0,NULL),(35,34,6,29,1,0,NULL),(36,32,6,30,1,0,NULL),(37,29,6,31,1,0,NULL),(38,25,6,32,1,0,NULL),(39,22,6,33,1,0,NULL),(40,19,6,34,1,0,NULL),(41,16,6,35,1,0,NULL),(42,5,6,36,1,0,NULL),(43,45,6,37,1,0,NULL),(44,41,6,38,1,0,NULL),(45,37,6,39,1,0,NULL),(46,34,6,40,1,0,NULL),(47,32,6,41,1,0,NULL),(48,29,6,42,1,0,NULL),(49,25,6,43,1,0,NULL),(50,22,6,44,1,0,NULL),(51,19,6,45,1,0,NULL),(52,16,6,46,1,0,NULL),(53,5,6,47,1,0,NULL),(54,45,6,48,1,0,NULL),(55,41,6,49,1,0,NULL),(56,37,6,50,1,0,NULL),(57,34,6,51,1,0,NULL),(58,32,6,52,1,0,NULL),(59,29,6,53,1,0,NULL),(60,25,6,54,1,0,NULL),(61,22,6,55,1,0,NULL),(62,19,6,56,1,0,NULL),(63,16,6,57,1,0,NULL),(64,5,6,58,1,0,NULL),(65,12,6,59,1,0,NULL),(66,7,6,60,1,0,NULL),(67,1,6,61,1,0,NULL),(68,44,6,62,1,0,NULL),(69,39,6,63,1,0,NULL),(70,36,6,64,1,0,NULL),(71,30,6,65,1,0,NULL),(72,26,6,66,1,0,NULL),(73,23,6,67,1,0,NULL),(74,18,6,68,1,0,NULL),(75,14,6,69,1,0,NULL),(76,9,6,70,1,0,NULL),(77,3,6,71,1,0,NULL),(78,46,6,72,1,0,NULL),(79,42,6,73,1,0,NULL),(80,38,6,74,1,0,NULL),(81,33,6,75,1,0,NULL),(82,28,6,76,1,0,NULL),(83,24,6,77,1,0,NULL),(84,20,6,78,1,0,NULL),(85,15,6,79,1,0,NULL),(86,12,6,80,1,0,NULL),(87,7,6,81,1,0,NULL),(88,9,7,82,1,0,NULL),(89,7,7,83,1,0,NULL),(90,3,7,84,1,1,'2026-10-21'),(91,1,7,85,1,0,NULL),(92,46,7,86,1,0,NULL),(93,44,7,87,1,0,NULL),(94,42,7,88,1,0,NULL),(95,39,7,89,1,0,NULL),(96,38,7,90,1,0,NULL),(97,36,7,91,1,0,NULL),(98,33,7,92,1,0,NULL),(99,30,7,93,1,0,NULL),(100,28,7,94,1,0,NULL),(101,26,7,95,1,0,NULL),(102,24,7,96,1,0,NULL),(103,23,7,97,1,0,NULL),(104,20,7,98,1,0,NULL),(105,18,7,99,1,0,NULL),(106,15,7,100,1,0,NULL),(107,14,7,101,1,0,NULL),(108,12,7,102,1,0,NULL),(109,9,7,103,1,0,NULL),(110,7,7,104,1,0,NULL),(111,3,7,105,1,0,NULL),(112,1,7,106,1,0,NULL),(113,46,7,107,1,0,NULL),(114,44,7,108,1,0,NULL),(115,42,7,109,1,0,NULL),(116,39,7,110,1,0,NULL),(117,38,7,111,1,0,NULL),(118,36,7,112,1,0,NULL),(119,33,7,113,1,0,NULL),(120,30,7,114,1,0,NULL),(121,28,7,115,1,0,NULL),(122,26,7,116,1,0,NULL),(123,24,7,117,1,0,NULL),(124,23,7,118,1,0,NULL),(125,20,7,119,1,0,NULL),(126,18,7,120,1,0,NULL),(127,15,7,121,1,0,NULL),(128,14,7,122,1,0,NULL),(129,12,7,123,1,0,NULL),(130,9,7,124,1,0,NULL),(131,7,7,125,1,0,NULL),(132,3,7,126,1,0,NULL),(133,1,7,127,1,0,NULL),(134,4,7,128,1,0,NULL),(135,47,7,129,1,0,NULL),(136,40,7,130,1,0,NULL),(137,31,7,131,1,0,NULL),(138,21,7,132,1,0,NULL),(139,13,7,133,1,0,NULL),(140,10,7,134,1,0,NULL),(141,6,7,135,1,1,'2026-10-21'),(142,2,7,136,1,0,NULL),(143,43,7,137,1,0,NULL),(144,35,7,138,1,0,NULL),(145,27,7,139,1,0,NULL),(146,17,7,140,1,0,NULL),(147,11,7,141,1,0,NULL),(148,8,7,142,1,0,NULL),(149,4,7,143,1,0,NULL),(150,47,7,144,1,0,NULL),(151,40,7,145,1,0,NULL),(152,31,7,146,1,0,NULL),(153,21,7,147,1,0,NULL),(154,13,7,148,1,0,NULL),(155,10,7,149,1,0,NULL),(156,6,7,150,1,0,NULL),(157,47,8,151,1,0,NULL),(158,40,8,152,1,0,NULL),(159,31,8,153,1,0,NULL),(160,21,8,154,1,0,NULL),(161,13,8,155,1,0,NULL),(162,10,8,156,1,0,NULL),(163,6,8,157,1,0,NULL),(164,2,8,158,1,0,NULL),(165,43,8,159,1,0,NULL),(166,35,8,160,1,0,NULL),(167,27,8,161,1,0,NULL),(168,17,8,162,1,0,NULL),(169,11,8,163,1,0,NULL),(170,8,8,164,1,0,NULL),(171,4,8,165,1,0,NULL),(172,47,8,166,1,0,NULL),(173,40,8,167,1,0,NULL),(174,31,8,168,1,0,NULL),(175,21,8,169,1,0,NULL),(176,13,8,170,1,0,NULL),(177,10,8,171,1,0,NULL),(178,6,8,172,1,0,NULL),(179,2,8,173,1,0,NULL),(180,2,9,174,1,0,NULL),(181,43,9,175,1,0,NULL),(182,35,9,176,1,0,NULL),(183,27,9,177,1,0,NULL),(184,17,9,178,1,0,NULL),(185,11,9,179,1,0,NULL),(186,8,9,180,1,0,NULL),(187,4,9,181,1,1,'2026-10-21'),(188,47,9,182,1,0,NULL),(189,40,9,183,1,0,NULL),(190,31,9,184,1,0,NULL),(191,21,9,185,1,0,NULL),(192,13,9,186,1,0,NULL),(193,10,9,187,1,0,NULL),(194,6,9,188,1,0,NULL),(195,2,9,189,1,0,NULL),(196,43,9,190,1,0,NULL),(197,35,9,191,1,0,NULL),(198,27,9,192,1,0,NULL),(199,17,9,193,1,0,NULL),(200,11,9,194,1,0,NULL),(201,8,9,195,1,0,NULL),(202,4,9,196,1,0,NULL),(203,9,9,197,1,0,NULL),(204,3,9,198,1,0,NULL),(205,46,9,199,1,0,NULL),(206,42,9,200,1,0,NULL),(207,38,9,201,1,0,NULL),(208,33,9,202,1,0,NULL),(209,28,9,203,1,0,NULL),(210,24,9,204,1,0,NULL),(211,20,9,205,1,0,NULL),(212,15,9,206,1,0,NULL),(213,12,9,207,1,0,NULL),(214,7,9,208,1,0,NULL),(215,1,9,209,1,0,NULL),(216,44,9,210,1,0,NULL),(217,39,9,211,1,0,NULL),(218,36,9,212,1,0,NULL),(219,30,9,213,1,0,NULL),(220,26,9,214,1,0,NULL),(221,23,9,215,1,0,NULL),(222,18,9,216,1,0,NULL),(223,14,9,217,1,0,NULL),(224,9,9,218,1,0,NULL),(225,3,9,219,1,0,NULL),(226,2,10,220,1,0,NULL),(227,43,10,221,1,0,NULL),(228,35,10,222,1,0,NULL),(229,27,10,223,1,0,NULL),(230,17,10,224,1,0,NULL),(231,11,10,225,1,0,NULL),(232,8,10,226,1,0,NULL),(233,4,10,227,1,0,NULL),(234,47,10,228,1,0,NULL),(235,40,10,229,1,0,NULL),(236,31,10,230,1,0,NULL),(237,21,10,231,1,0,NULL),(238,13,10,232,1,0,NULL),(239,10,10,233,1,0,NULL),(240,6,10,234,1,0,NULL),(241,2,10,235,1,0,NULL),(242,43,10,236,1,0,NULL),(243,35,10,237,1,0,NULL),(244,27,10,238,1,0,NULL),(245,17,10,239,1,0,NULL),(246,11,10,240,1,0,NULL),(247,8,10,241,1,0,NULL),(248,4,10,242,1,0,NULL),(269,40,10,268,1,0,NULL),(270,34,10,269,1,0,NULL),(271,34,10,270,1,0,NULL),(272,34,10,271,1,0,NULL),(273,34,10,272,1,0,NULL),(274,34,10,273,1,0,NULL),(275,34,10,274,1,0,NULL),(276,34,10,275,1,0,NULL),(277,34,10,276,1,0,NULL),(278,34,10,277,1,0,NULL),(279,34,10,278,1,0,NULL),(280,34,10,279,1,0,NULL),(281,34,10,280,1,0,NULL),(282,34,10,281,1,0,NULL),(283,34,10,282,1,0,NULL),(284,34,10,283,1,0,NULL),(285,41,1,284,1,0,NULL),(286,41,4,285,10,0,NULL),(287,41,1,285,8,0,NULL),(288,41,4,286,10,0,NULL),(289,41,1,286,8,0,NULL),(290,41,4,287,10,0,NULL),(291,41,1,287,8,0,NULL),(292,41,4,288,10,0,NULL),(293,41,1,288,8,0,NULL),(294,41,4,289,10,0,NULL),(295,41,1,289,8,0,NULL),(296,19,10,290,1,0,NULL);


-- Dump completed on 2026-10-25 15:08:33


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `comissao`
--

DROP TABLE IF EXISTS `comissao`;
CREATE TABLE `comissao` (
  `id_comissao` int NOT NULL AUTO_INCREMENT,
  `id_profissional` int NOT NULL,
  `id_sessao` int NOT NULL,
  `valor_comissao` decimal(10,2) NOT NULL,
  `data_calculo` date NOT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'Pendente',
  PRIMARY KEY (`id_comissao`),
  UNIQUE KEY `uq_comissao_sessao` (`id_sessao`),
  CONSTRAINT `fk_comissao_profissional` FOREIGN KEY (`id_profissional`) REFERENCES `profissional` (`id_profissional`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_comissao_sessao` FOREIGN KEY (`id_sessao`) REFERENCES `sessao_atendimento` (`id_sessao`) ON DELETE RESTRICT ON UPDATE CASCADE
);

--
-- Dumping data for table `comissao`
--

INSERT INTO `comissao` VALUES (1,2,1,63.00,'2026-08-10','Paga'),(2,2,2,63.00,'2026-08-24','Pendente'),(3,3,3,48.75,'2026-07-15','Paga');


-- Dump completed on 2026-10-25 15:08:32


-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: bellapelle
-- ------------------------------------------------------
-- Server version	8.0.46


--
-- Table structure for table `log_auditoria`
--

DROP TABLE IF EXISTS `log_auditoria`;
CREATE TABLE `log_auditoria` (
  `id_log` bigint NOT NULL AUTO_INCREMENT,
  `id_usuario` int NOT NULL,
  `acao` varchar(50) NOT NULL,
  `entidade_afetada` varchar(100) NOT NULL,
  `id_entidade` int DEFAULT NULL,
  `data_hora` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `detalhes` text,
  PRIMARY KEY (`id_log`),
  CONSTRAINT `fk_log_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`) ON DELETE RESTRICT ON UPDATE CASCADE
);

--
-- Dumping data for table `log_auditoria`
--



-- Dump completed on 2026-10-25 15:08:33


SET REFERENTIAL_INTEGRITY TRUE;

