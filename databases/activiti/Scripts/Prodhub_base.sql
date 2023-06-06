SHOW search_path;
SET search_path TO prodhub;

-- GEOLOCATION START ---

-- country
-- drop table prodhub.country;
create table prodhub.country(id serial PRIMARY KEY, name VARCHAR (1000) not null, createddate TIMESTAMP not null DEFAULT CURRENT_DATE);

-- state table
-- drop table prodhub.state;
create table prodhub.state(id serial primary key, name varchar(1000) not null, countryid INT,
	createddate TIMESTAMP not null DEFAULT CURRENT_DATE,
	constraint fk_country foreign key(countryid) references country(id));

select * from country;
select * from prodhub.state;

create table prodhub.city(id serial primary key, name varchar(1000) not null, stateid INT, createddate timestamp
not null default current_date, constraint fk_state foreign key(stateid) references prodhub.state(id));

select * from prodhub.city;


create table prodhub.businessunitent(id serial primary key, 
name varchar(1000) not null, geolocationid varchar(1000) not null, businesstype varchar(1000) not null, 
cityid int not null, createddate timestamp default current_date, constraint fk_city foreign key(cityid)
references prodhub.city(id));


-- GEOLOCATION END ---


-- CUSTOMER Data START --
create table prodhub.customerinfo(id serial primary key, name varchar(1000) not null, cellnumber varchar(15) not null,
cityid int not null, zipcode varchar(6) not null, createddate timestamp default current_date, deleted boolean default false,
constraint fk_city_cust foreign key(cityid) references prodhub.city(id)
);

create table prodhub.deliveryaddress(id serial primary key, title varchar(15), flname varchar(1000) not null,
housenumber varchar(10), streetname varchar(1000), nearbylandmark varchar(1000), cityid int default -1, zipcode
varchar(8) not null, cityname varchar(200) not null
);


create table prodhub.tradeinfo(id serial primary key, 
quantity int default -1, customerid int not null, businessentid int not null, 
productid int not null, ordercreateddate timestamp default current_date, orderexpecteddate timestamp 
default current_date, orderquantity decimal default 0.0, unittype varchar(20), ordertotal decimal default 0.0,
todeliveraddress varchar(5000), miscellaneous varchar(2000));

create table prodhub.product(id serial primary key, productname varchar(1000) not null, manufacturername varchar(1000));

create table prodhub.productbusinesscatalogue(id serial primary key, businessunitid int not null,
quantity int not null default 0, inventorydate timestamp default current_date, additionalnotes varchar(3000),
constraint fk_prodbusiness_link foreign key(businessunitid) references prodhub.businessunitent(id));

alter table prodhub.productbusinesscatalogue add column productid int not null;
alter table prodhub.productbusinesscatalogue add constraint fk_buscat_productid foreign key(productid) references prodhub.product(id);

select * from prodhub.product;

select * from prodhub.state;
select * from prodhub.country;



