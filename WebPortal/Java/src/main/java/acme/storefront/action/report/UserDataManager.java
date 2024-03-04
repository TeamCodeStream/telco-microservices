package acme.storefront.action.report;

import com.newrelic.api.agent.Trace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDataManager {

    public static class Address {
        private String street1;
        private String zip;
        private String city;
        private String state;

        public Address(String street1, String zip, String city, String state) {
            this.street1 = street1;
            this.zip = zip;
            this.city = city;
            this.state = state;
        }

        public String getStreet1() {
            return street1;
        }

        public void setStreet1(String street1) {
            this.street1 = street1;
        }

        public String getZip() {
            return zip;
        }

        public void setZip(String zip) {
            this.zip = zip;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return "Address{" +
                    "street1='" + street1 + '\'' +
                    ", zip='" + zip + '\'' +
                    ", city='" + city + '\'' +
                    ", state='" + state + '\'' +
                    '}';
        }
    }

    public static class Phone {
        private String countryCode;
        private String number;

        public Phone(String countryCode, String number) {
            this.countryCode = countryCode;
            this.number = number;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        @Override
        public String toString() {
            return "Phone{" +
                    "countryCode='" + countryCode + '\'' +
                    ", number='" + number + '\'' +
                    '}';
        }
    }

    public static class User {
        private String firstName;
        private String lastName;
        private Phone phone;
        private Address address;

        public User(String firstName, String lastName, Phone phone, Address address) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.phone = phone;
            this.address = address;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public Phone getPhone() {
            return phone;
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return "User{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", phone=" + phone +
                    ", address=" + address +
                    '}';
        }
    }

    public static class UserView {
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private String state;

        public UserView(String firstName, String lastName, String phoneNumber, String state) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.phoneNumber = phoneNumber;
            this.state = state;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        @Override
        public String toString() {
            return "UserView{" +
                    "firstName='" + firstName + '\'' +
                    ", lastName='" + lastName + '\'' +
                    ", phoneNumber='" + phoneNumber + '\'' +
                    ", state='" + state + '\'' +
                    '}';
        }
    }

    static class UserData {
        Map<String, User> userDb;

        UserData() {
            userDb = new HashMap<>();
            userDb.put("abcd1",
                    new User("Joe",
                            "Malaci",
                            new Phone("1", "888-444-2222"),
                            new Address("1234 W. Here Pl", "33232", "Boston", "MA")));
            userDb.put("abcd2",
                    new User("Jane", "Todd", null, new Address("1234 W. Here Pl", "33232", "Boston", "MA")));
            userDb.put("abcd3",
                    new User("Jane", "Todd", null, new Address("1234 E. There Pl", "13131", "Boulder", "CO")));
        }
    }

    private final UserData userData = new UserData();

    @Trace
    public List<UserView> getUserViewByState(String state) {
        List<UserView> userViewList = new ArrayList<>();
        for (User user : userData.userDb.values()) {
            if (user.getAddress().getState().equals(state)) {
                userViewList.add(new UserView(user.getFirstName(),
                        user.getLastName(),
                        "+" + user.getPhone().getCountryCode() + " " + user.getPhone().getNumber(),
                        user.getAddress().getState()));
            }
        }
        return userViewList;
    }

    @Trace
    public UserView getUserById(String userId) {
        User user = userData.userDb.get(userId);
        return new UserView(user.getFirstName(),
                user.getLastName(),
                "+" + user.getPhone().getCountryCode() + " " + user.getPhone().getNumber(),
                user.getAddress().getState());
    }
}
