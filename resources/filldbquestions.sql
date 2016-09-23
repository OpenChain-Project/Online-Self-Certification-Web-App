insert into section(name, title) values('G1','Know Your FOSS Responsibilities');
insert into question(name, question, type, correct_answer, section_id) values(
        '1.1', 'Do you have rules that govern FOSS license compliance of the Supplied Software distribution?',
        'YES_NO','Yes', (select id from section where name='G1'));