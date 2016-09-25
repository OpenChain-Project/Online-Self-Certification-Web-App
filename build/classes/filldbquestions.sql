insert into spec(version) values('1.0');
insert into section(name, title, spec_version) values('G1','Know Your FOSS Responsibilities', (select id from spec where version='1.0'));
insert into question(number, question, type, correct_answer, section_id) values(
        '1.1', 'Do you have rules that govern FOSS license compliance of the Supplied Software distribution?',
        'YES_NO','Yes', (select id from section where name='G1'));
        
insert into section(name, title, spec_version) values('G2','Assign Responsibility for Achieving Compliance', (select id from spec where version='1.0'));
insert into question(number, question, type, correct_answer, section_id) values(
        '2.1', 'Have you assigned an individual responsible for managing internal FOSS compliance?',
        'YES_NO','Yes', (select id from section where name='G2'));

     