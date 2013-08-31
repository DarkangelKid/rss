We agree on the Allman style but here are some further propositions to allign our code style to.
I am using github markup of 4 spaces for the initial indent just for the readme file.

I read through
```
http://en.wikipedia.org/wiki/Programming_style#Code_appearance &
http://en.wikipedia.org/wiki/Coding_conventions ,
```
considered the arguments and figured this style would be best.

Tell me if you agree or think something should be different, then we will convert the code over
time.

```
Edit:Preferences:Files:Replace tabs with spaces to checked.
Edit:Preferences:Editor:Indentation - set width to 3 and type to spaces.
Edit:Preferences:Editor:Display     - set column to 96.
```

This allows allignment using indentation to be consistant. It also makes our github code
readable. Tab now inserts three spaces. Backspace will also know it was a tab and delele 3.

If parameters to a function go over 96 characters width, aligned with single spaces.

```
int foobar2(int qux,
            int quux,
            int quuux);
```

Use vertical alignment to the longest line, arguments against this are more noise in revision
control and search-and-replace will mess it up so it takes more effort to keep this style.
```
String[] willow = array('foo', 'bar', 'baz', 'quux');
String   hi     = 0;
String   hello  = 1;
```


Use a single space between each charater in a comparision.
```
if( 8 = max ), looks clearer than if(8 = max)
```
for functions that return booleans, no spaces.
```
if(main.TXT.equals(".txt"))
```

Use left-hand comparisons because if = is typed instead of ==,
```
if( 2 = a ); /* Throws a compile-time error. */
if( a = 2 ); /* Sets a to 2. */
```

For conditions that go over 96 characters width, align using the minimum number of tabs.
```
if( a == b ||
   a == c   )
{
   ...
};

```


Even for single line conditions and loops, require curly brackets.
```
for(i = 0; i < array.length; i++)
{
   i++;
};

```

This format avoids error such as:
```
for(i = 0; i < array.length; i++);
   i++;
```

and,
```
for(i = 0; i < array.length; i++)
   i++;
   line = i;

```

Use trailing commas for arrays with long elements.
Keep the last comma so editing the array is one change in revision control.
```
String[] =
{
   "item1",
   "item2",
   "item3",
};
```
