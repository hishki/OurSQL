package parser;

import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

import database.Database;
import dbTypes.DBEnumTypes;
import dbTypes.DBTypes;
import table.Action;
import table.DBTable;
import table.ForeignKeyInv;

public class CreateTableType extends ParserTypes {

	String tableName;
	HashMap<String, Integer> schema;
	Vector<String> names;
	Vector<DBTypes> types;
	Vector<ForeignKeyInv> fks;
	String PK;
	
	public Vector<ForeignKeyInv> getFKs() {
		return fks;
	}
	
	public String getPK() {
		return PK;
	}

	public String getTableName() {
		return tableName;
	}

	public Vector<DBTypes> getTypes() {
		return types;
	}

	public Vector<String> getNames() {
		return names;
	}

	public HashMap<String, Integer> getSchema() {
		return schema;
	}

	public CreateTableType() {
		this.commandType = CommandTypes.CREATE_TABLE;
		schema = new HashMap<>();
		names = new Vector<String>();
		types = new Vector<DBTypes>();
	}

	@Override
	public void parse() {
		int columnsStart = command.indexOf('(');
		int columnsEnd = command.indexOf(')');
		Scanner scanner = new Scanner(command.substring(columnsStart+1, columnsEnd));
		scanner.useDelimiter(ParseCommand.DELIMS);
//		scanner.next(); // create
//		scanner.next(); // table

		tableName = scanner.next();

		while(scanner.hasNext()){
			String key = scanner.next();
			String type = scanner.next();
			Class<?> c;
			DBTypes dbType = null;
			try {
				c = DBEnumTypes.valueOf(type).getTypeClass();
				dbType = (DBTypes) c.newInstance();
			} catch ( InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			names.add(key);
			types.add(dbType);
			schema.put(key, names.size()-1);
		}
		scanner.close();

		scanner = new Scanner(command.substring(columnsEnd+1));
		scanner.next(); // PRIMARY
		scanner.next(); // KEY
		PK = scanner.next();

		while(scanner.hasNext()) {
			scanner.next(); // FOREIGN
			scanner.next(); // KEY
			String fkcol = scanner.next();
			scanner.next(); // REFERENCES
			String tableName = scanner.next();
			scanner.next(); // ON
			scanner.next(); // DELETE
			Action onDelete = Action.getValue(scanner.next());
			scanner.next(); // ON
			scanner.next(); // UPDATE
			Action onUpdate = Action.getValue(scanner.next());
			
			fks.add(new ForeignKeyInv(tableName, onUpdate, onDelete));
		}

	}

	@Override
	public String action(Database database) {
		database.addTables(tableName, new DBTable(this, database));
		return "TABLE CREATED";
	}



}
