Change Log
==========

## Version 1.1.0

_2014-09-16_

 * Added FieldType REAL - Fixes [#12](https://github.com/Triple-T/simpleprovider/issues/12)
 * Fixed pluralization of table names that end on _vowel + y_ - Fixes [#10](https://github.com/Triple-T/simpleprovider/issues/10)

> Note: This change might be incompatible if you previously relied on the wrong pluralization. E.g. if you have defined a schema class called `Journey`, SimpleProvider has created a table called `journeies` for you. You have to make sure to manually set that name again via the `value`-property in the `@Table`-Annotation

## Version 1.0.1

_2014-07-10_

 * More ways to extend the default behaviour of `AbstractProvider` in derived classes:
  * Added `onUpgrade()` for custom schema updates.
  * Removed `final` modifier from `onCreate()`.


## Version 1.0.0

_2014-05-16_

Initial release.
