AWARE Plugin: Device Discovery
==========================

This plugin uses NSD to discover the devices on the similar network and uses CoAP to send sensor data.

# Settings
Parameters adjustable on the dashboard and client:
- **status_plugin_template**: (boolean) activate/deactivate plugin

# Broadcasts
**ACTION_AWARE_PLUGIN_TEMPLATE**
Broadcast ..., with the following extras:
- **value_1**: (double) amount of time turned off (milliseconds)

# Providers
##  Template Data
> content://device_discovery.provider.xxx/plugin_template

Field | Type | Description
----- | ---- | -----------
_id | INTEGER | primary key auto-incremented
timestamp | REAL | unix timestamp in milliseconds of sample
device_id | TEXT | AWARE device ID
