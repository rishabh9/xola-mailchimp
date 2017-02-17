// Execute this script only if you're upgrading to v0.2.2 or later.
// Not required for a fresh db.

use mailchimp;

// Add type data to preference fields of the installations
var cursor = db.getCollection('installations').find({});
while (cursor.hasNext()) {
    var obj1 = cursor.next();
    var prefs = obj1.preferences;
    if (prefs) {
        prefs.forEach(function (item) {
            item.values.forEach(function (vals) {
                vals['type'] = 'string';
            });
        });
    }
    db.getCollection('installations').update({'_id': obj1._id}, obj1);
}
