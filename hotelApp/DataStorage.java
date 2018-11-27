package hotelApp;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.File;

/**
 * Class that stores all accounts in memory, and offloads data into files for long-term storage
 * @author Nicholas Papano
 * Date created: 11/13/2018
 */
public class DataStorage
{
    private ArrayList<Account> accounts;
    private ArrayList<Room> rooms;
    private File reservationFile, accountFile;
    private static DateTimeFormatter date_format = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    /**
     * Public ctor that initializes the database. It populates its data structures with information from the files
     */
    public DataStorage()
    {
        accounts = new ArrayList<>();
        rooms = new ArrayList<>();

        // Hard coded list of rooms
        rooms.add(new Room(100, 100));
        rooms.add(new Room(110, 100));
        rooms.add(new Room(120, 100));
        rooms.add(new Room(130, 100));
        rooms.add(new Room(140, 100));
        rooms.add(new Room(150, 100));
        rooms.add(new Room(160, 100));
        rooms.add(new Room(170, 100));
        rooms.add(new Room(180, 100));
        rooms.add(new Room(190, 100));
        rooms.add(new Room(500, 300));
        rooms.add(new Room(510, 300));
        rooms.add(new Room(520, 300));
        rooms.add(new Room(530, 300));
        rooms.add(new Room(540, 300));
        rooms.add(new Room(550, 300));
        rooms.add(new Room(560, 300));
        rooms.add(new Room(570, 300));
        rooms.add(new Room(580, 300));
        rooms.add(new Room(590, 300));

        reservationFile = new File("hotelApp/reservations.txt");
        accountFile = new File("hotelApp/accounts.txt");

        populateFromFile();
    }


    /**
     * Populates local data structures from files
     *
     * @return true if successful, false if it encounters an error
     */
    public boolean populateFromFile()
    {
        try
        {
            Scanner accSc = new Scanner(accountFile);
            Scanner resSc = new Scanner(reservationFile);
            String nextLn;
            int lineNum = 0;

            // Get accounts from file
            while (accSc.hasNextLine())
            {
                nextLn = accSc.nextLine();
                String[] accInfo = nextLn.split(", ");
                /*
                format for account from file is:
                name, username, pw, isManager
                 */
                boolean isManager = Boolean.parseBoolean(accInfo[3]);
                if (isManager)
                    accounts.add(new ManagerAccount(accInfo[0], accInfo[1], accInfo[2], lineNum++));
                else
                    accounts.add(new GuestAccount(accInfo[0], accInfo[1], accInfo[2], lineNum++));
            }

            // get reservations from file
            while (resSc.hasNextLine())
            {
                nextLn = resSc.nextLine();
                String[] resInfo = nextLn.split(", ");

                Account resAcc = null;
                for (Account a : accounts)
                {
                    if (a.getUsername().equals(resInfo[0]))
                    {
                        resAcc = a;
                        break;
                    }
                }
                if (resAcc == null)
                {
                    System.out.println("Couldn't find account by name: " + resInfo[0]);
                    continue;
                }
                Room resRoom = getRoomByNumber(Integer.parseInt(resInfo[1]));
                if (resRoom == null)
                {
                    System.out.println("Couldn't find Room by number: " + resInfo[1]);
                    continue;
                }
                LocalDate startDate = LocalDate.parse(resInfo[2], date_format);
                LocalDate endDate = LocalDate.parse(resInfo[3], date_format);
                Reservation res = new Reservation(resAcc, resRoom, new TimeInterval(startDate, endDate));
                resAcc.addReservation(res);

            }
            accSc.close();
            resSc.close();
        }
        catch (FileNotFoundException e)
        {
            System.out.println("FileNotFoundException while parsing files for database population");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Saves all data stored in the data structures to files for long-term storage
     *
     * @return true if successful, false if an error occurs
     */
    public boolean saveToFile()
    {
        try
        {
            FileWriter accWriter = new FileWriter(accountFile);
            FileWriter resWriter = new FileWriter(reservationFile);
            for (Account a : accounts)
            {
                accWriter.write(a.getName() + ", " + a.getUsername() + ", " + a.getPassword() + ", " + a.isManager() + "\n");
                for (Reservation r : a.getReservations())
                {
                    LocalDate start = r.getTime().getStartTime();
                    LocalDate end = r.getTime().getEndTime();

                    resWriter.write(r.getAttachedAccount().getUsername() + ", " + r.getRoom().getRoomNumber() + ", " + start.format(date_format) + ", " + end.format(date_format)+ "\n");
                }
            }
            accWriter.close();
            resWriter.close();
        }
        catch (IOException e)
        {
            System.out.println("IOException while saving contents of database to file");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Gets an account from the data structure given a username
     *
     * @param username the username of the account
     * @return the account if the account exists, null if the account does not exist
     */
    public Account getAccountByUserName(String username)
    {
        for (Account a : accounts)
        {
            if (a.getUsername().equals(username))
                return a;
        }
        return null;
    }

    /**
     * Gets a room from the list of rooms, given it's number
     *
     * @param num the number of the room to retrieve
     * @return the room if a room with that number exists, null if it does not
     */
    public Room getRoomByNumber(int num)
    {
        int roomNum = num / 100;
        int i = -1;
        if (roomNum == 1)
        {
            i = 0;
        } else if (roomNum == 5)
        {
            i = 10;
        }
        if (i == -1)
            return null;

        int j = i + 10;
        for (; i < j; i++)
        {
            if (rooms.get(i).getRoomNumber() == num)
                return rooms.get(i);
        }
        return null;
    }

    /**
     * Validates that the inputted username/password combo match an existing account's username/password
     *
     * @param username the username of the account
     * @param password the password of the account
     * @return true if correct username/password combo, false otherwise
     */
    public boolean validate(String username, String password)
    {
        for (Account a : accounts)
        {
            if (a.getUsername().equals(username) && a.getPassword().equals(password))
                return true;
        }
        return false;
    }

    /**
     * Gets a list of all accounts stored in the local data structure, not from the file
     *
     * @return an arraylist of all accounts
     */
    public ArrayList<Account> getAccounts()
    {
        return accounts;
    }

    /**
     * Gets a list of all rooms, both available and not available
     *
     * @return an arraylist of all rooms
     */
    public ArrayList<Room> getRooms()
    {
        return rooms;
    }

    /**
     * Adds a new account to the local data structure if the account username is unique
     *
     * @param newAcc the account to be added
     * @return true if username is unique and the data is added, false if username is not unique (account will NOT be added)
     */
    public boolean addAccount(Account newAcc)
    {
        for (Account a : accounts)
        {
            if (a.getUsername().equals(newAcc.getUsername()))
                return false;
        }
        accounts.add(newAcc);
        return true;
    }

    /**
     * Gets a list of rooms that are not occupied during the TimeInterval passed in
     *
     * @param duration the TimeInterval of which the room needs to be available
     * @return a list of rooms that are free for the entire duration
     */
    public ArrayList<Room> getAvailableRooms(TimeInterval duration)
    {
        //TODO: MAKE SURE TO TEST
    	ArrayList<Room> available = new ArrayList<>(rooms);
        for (Account a : accounts)
        {
            for (Reservation r : a.getReservations())
            {
                if (r.getTime().overlap(duration))
                    available.remove(r.getRoom());
            }
        }
    	return available;
    }
}
