drop database if exists Shoestore;
create database Shoestore;
use Shoestore;

create table City(
	ID int not null auto_increment primary key,
	Name varchar(100) not null unique
);

create table Customer(
	ID int not null auto_increment primary key,
	Firstname varchar(100) not null,
	Lastname varchar(100) not null,
	CityID int,
	Address varchar(100) not null,
	Password varchar(100) not null,
	foreign key(CityID) references City(ID) on delete set null 
);

create table Category(
	ID int not null auto_increment primary key,
	Name varchar(100) not null unique
);

create table Shoe(
	ID int not null auto_increment primary key,
	Brand varchar(100) not null,
	Model varchar(100) not null,
	Price float not null
);

create index idx_shoeModel on Shoe (Model);

create table ShoeClassification(
	CategoryID int not null,
	ShoeID int not null,
	foreign key (CategoryID) references Category(ID) on delete cascade,
	foreign key (ShoeID) references Shoe(ID) on delete cascade,
	primary key (CategoryID, ShoeID)
);

create table Color(
	ID int not null unique auto_increment,
	Name varchar(100) not null unique,
	primary key(ID, Name)
);

create table Size(
	ID int not null unique auto_increment,
	ShoeSize int not null unique,
	primary key(ID, ShoeSize)
);

create table Inventory(
	ID int not null unique auto_increment,
	ShoeID int,
	ColorID int not null,
	SizeID int not null,
	Quantity int not null,
	foreign key (ShoeID) references Shoe(ID) on delete cascade,
	foreign key (ColorID) references Color(ID) on delete cascade,
	foreign key (SizeID) references Size(ID) on delete cascade,
	primary key (ID, ShoeID, SizeID, ColorID)
);

create table Purchase(
	ID int not null auto_increment primary key,
	CustomerID int,
	Paid boolean default False,
	Purchase_Date Date default (current_date),
	foreign key (CustomerID) references Customer(ID) on delete set null 
);

create table PurchaseInfo(
	InventoryID int not null,
	PurchaseID int not null,
	Quantity int not null,
	foreign key(InventoryID) references Inventory(ID) on delete cascade, 
	foreign key(PurchaseID) references Purchase(ID) on delete cascade,
	primary key (InventoryID, PurchaseID)
);

create table OutOfStock(
	InventoryID int not null primary key,
	Time_Stamp Timestamp not null default current_timestamp,
	foreign key (InventoryID) references Inventory(ID) on delete cascade
);

create view Purchase_Cost as select Purchase.CustomerID, Purchase.Paid, Purchase.Purchase_Date, format(sum(Shoe.Price * PurchaseInfo.Quantity),2) as Cost from Purchase
	join PurchaseInfo on PurchaseInfo.PurchaseID = Purchase.ID
	join Inventory on PurchaseInfo.InventoryID = Inventory.ID
	join Shoe on Inventory.ShoeID = Shoe.ID
	group by Purchase.CustomerID, Paid, Purchase_Date;

delimiter //

create procedure restockShoe(ShoeToStockID int, amount int, shoeSizeID int)
	begin
		if exists(select * from Inventory where ShoeID = ShoeToStockID and SizeID = shoeSizeID) then
			update Inventory set Quantity = Quantity + amount where ShoeID = ShoeToStockID and SizeID = shoeSizeID;
		else
			insert into Inventory(ShoeID, ColorID, SizeID, Quantity) values
			(ShoeToStockID, 1, shoeSizeID, amount),(ShoeToStockID, 2, shoeSizeID, amount),(ShoeToStockID, 3, shoeSizeID, amount),(ShoeToStockID, 4, shoeSizeID, amount);
		end if;
	end//

create procedure AddToCart(Customer_ID int, Purchase_ID int, Inventory_ID int, shoeQuantity int)
	begin
		declare temp_purchase int default Purchase_ID;

		declare exit handler for sqlexception
			begin
				show errors;
				rollback;
			end;

		start transaction;

			if (Purchase_ID is null) then
				if not exists (select ID from Purchase where CustomerID = Customer_ID and Paid = False) then
					insert into Purchase(CustomerID) values (Customer_ID);
					select last_insert_id() into temp_purchase;
				else
					select ID from Purchase where CustomerID = Customer_ID and Paid = False into temp_purchase;
				end if;

			elseif not exists (select ID from Purchase where Purchase_ID = ID and CustomerID = Customer_ID and Paid = False) then
				signal sqlstate '45000' set message_text = 'Incorrect order number provided, unpaid order in cart.', mysql_errno = 2000;
			end if;

			if exists (select ID from Inventory where ID = Inventory_ID) then
				if ((select Quantity from Inventory where ID = Inventory_ID) >= shoeQuantity and shoeQuantity > 0) then
					update Inventory set Quantity = Quantity - shoeQuantity where ID = Inventory_ID;
					
					if exists (select PurchaseID from PurchaseInfo where PurchaseID = temp_purchase and InventoryID = Inventory_ID) then
						update PurchaseInfo set Quantity = Quantity + shoeQuantity where PurchaseID = temp_purchase and InventoryID = Inventory_ID;
					else 
						insert into PurchaseInfo(InventoryID, PurchaseID, Quantity) values (Inventory_ID, temp_purchase, shoeQuantity);
					end if;
					select 'Product added to cart.';
				else signal sqlstate '45000' set message_text = 'Invalid quantity, not enough products in stock.', mysql_errno = 2001;
				end if;
			else signal sqlstate '45000' set message_text = 'No such product in inventory.', mysql_errno = 2002;
			end if;

		commit;
	end//

create procedure finalizePurchase(Customer_ID int, Purchase_ID int, Pay boolean, out returnMessage varchar(100))
	begin
		declare temp_purchase int default Purchase_ID;

		declare exit handler for sqlexception
			begin
				get diagnostics condition 1 returnMessage = message_text;			
				rollback;
			end;

		start transaction;
		
        	if (Purchase_ID is null) then
			select ID from Purchase where CustomerID = Customer_ID and Paid = False into temp_purchase;
		end if;
        
		if not exists (select * from Purchase where CustomerID = Customer_ID and ID = temp_purchase and Paid = False) then
			signal sqlstate '45000' set message_text = 'Faulty purchase information, try again.';
		end if;

		if (Pay) then
			update Purchase set Paid = True where ID = temp_purchase;
			set returnMessage = 'Order payment processed successfully.';
		else
			delete from Purchase where ID = temp_purchase;
			set returnMessage = 'Order deleted successfully.';
		end if;

		commit;
	end//

create trigger alertOutOfStock after update on Inventory
	for each row
	begin
		if (new.Quantity = 0) then
			insert into OutOfStock(InventoryID) values (new.ID);
		end if;
	end//

insert into City(Name) values ('Stockholm'), ('Malmö'), ('Uppsala'), ('Göteborg'),('Sollefteå');

insert into Customer(Firstname, Lastname, CityID, Address, Password) values
	('Mike', 'Daniels', 1, '1st Street','secretPassword'),
	('Ace', 'Ventura', 2, '1st Road','secretPassword'),
	('Neo', 'TheOne', 3, '1st Way','secretPassword'),
	('Alice', 'IsLost', 4, '1st Wonderland','secretPassword'),
	('Hermione', 'Granger', 1, 'Privet Drive','secretPassword'),
	('Lady', 'Gaga', 2, '10th Street','secretPassword'),
	('Google', 'Inc', 3, '@gmail.com','secretPassword');

insert into Category(Name) values ('Running'), ('Walking'), ('Dress Shoe'), ('Boots'), ('Training');

insert into Shoe(Brand, Model, Price) values 
	('Nike', 'Airmax', 599),('Reebok', 'SuperSneaker', 899),('Adidas', 'CoolRun', 1099),
	('Salomon', 'Fancy', 1599),('Nike', 'Starlace', 1249.90),('Adidas', 'DressNice', 2095.9),
	('Salomon', 'Hiker', 1789.8),('Ecco', 'Sandal', 1295);

insert into ShoeClassification(CategoryID, ShoeID) values (2,1),(1,2),(1,3),(3,4),(5,5),(3,6),(4,7),(5,8);

insert into Color(Name) values ('Black'),('White'),('Red'),('Blue');

insert into Size(ShoeSize) values (36),(38),(40),(42);

call restockShoe(1, 50, 1);
call restockShoe(3, 50, 2);
call restockShoe(8, 50, 3);
call restockShoe(8, 50, 4);
call restockShoe(2, 80, 1);
call restockShoe(2, 40, 2);
call restockShoe(4, 80, 3);
call restockShoe(4, 60, 4);
call restockShoe(5, 80, 1);
call restockShoe(5, 100, 2);
call restockShoe(6, 100, 3);
call restockShoe(7, 30, 4);
call restockShoe(8, 20, 2);
call restockShoe(3, 50, 2);
call restockShoe(1, 50, 3);

call addtocart(1,null,2,2);
call addtocart(1,null,1,2);
call addtocart(2,null,1,3);
call addtocart(3,null,4,2);
call addtocart(4,null,3,5);
call addtocart(5,null,6,2);
call addtocart(6,null,7,2);
call addtocart(6,null,10,3);
call addtocart(7,null,1,2);
call addtocart(7,null,8,25);

call finalizepurchase(1,null,True,@msg);
call finalizepurchase(2,null,True,@msg);
call finalizepurchase(3,null,True,@msg);
call finalizepurchase(4,null,True,@msg);
call finalizepurchase(5,null,True,@msg);
call finalizepurchase(6,null,True,@msg);

