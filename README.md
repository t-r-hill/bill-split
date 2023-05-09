# Bill Split

Bill split is a java web application to allow groups of people to easily track and split 
expenses between the members of the group

## Dependencies

#### Java11 jdk
#### MySQL database
The url, username and password will need to be added to the application.properties file
#### Redis cache
The host and port will need to be added to the application.properties file
#### [Mailersend](https://www.mailersend.com/)
The api key and an email template id for the invite email will need to be added to 
application.properties
#### [Currencyapi](https://currencyapi.com/)
The api key will need to be added to application.properties

## Installation

Can be built and deployed as a jar or war through gradle

## Usage

The application runs as a web app allowing users to register for an account and login 
using their created credentials

Once logged in - a user can:

### Group management
* Create a group to track and split expenses within
* Join other groups by entering a group invite code or clicking on a group invite link
* Invite others to join a group by sending them a group invite link, invite code or 
entering their email address so that they are sent an invite link within an email
* The creator of agroup is assigned as a group admin. Admin priveleges grant:
	* Invite/remove users from a group
	* Assign/remove others users as admin
	* Edit/delete expenses submitted by other users within a group
	* Initiate process to split expenses equally between all group members and 
calculate payments between group members

 ### Expense management
* A user can create an expense within a group. It is created with a currency and will be 
automatically converted to the 'base currency' of the group 
* Before the expense has been 'split' with the rest of the group, the expense can be 
edited by the user whom created it
* Expenses can be viewed within a group and within an expense search function

### Payment management
* An admin of a group may 'Calculate payments' which will  sum amounts owed to each 
group member for all current expense which have not already been 'split' and create 
payments between group members so that everyone will have spent the same amount in total
* All payments are calculated in the base currency of the group
* A payment is initially created in a 'Not paid' status and may be marked as 'Pending' 
when a user has made the payment, then 'Confirmed' when a user has received the payment
