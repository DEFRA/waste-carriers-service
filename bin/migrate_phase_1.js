/*
This script migrates existing lower tier registrations from phase 1.

Execute this script from a Mongo shell, for example:

$ mongo waste-carriers -u <user> -p <password> migrate_phase_1.js
*/

print("*** Waste Carriers Data Migration Script ***")
var count = db.registrations.count()
print("Number of registrations in the database: " + count)
print("About to update - adding 'tier' property...")
db.registrations.update({'tier':{$exists:false}},{$set:{'tier':'LOWER'}},{multi:true})
print("Update completed.")
