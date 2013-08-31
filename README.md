We agree on the Allman style but here are some further propositions to allign our code style to.

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
Edit:Preferences:Editor:Display     - set column to 80.
```

Using spaces allows allignment using indentation to be consistant. It also makes our github code
readable. Tab now inserts three spaces. Backspace will also know it was a tab and delete three.

When commenting, use /* style */. When commenting out single lines of code, use //.
For commenting out multiple lines, use /* hi = 2 */.

Always leave an empty line above /* */ comments.

The 80 column argument seems to be pretty much standard.
It is the width of monospace on A4. Books tend to be 66 characters.
It is for readability because it is hard to read wide lines.
Possible to have two pages open side by side on most monitors.
Some terminals max at 80 chars.
Code can be read on github without a horizontal bar.
Lines over 80 characters tend to be a sign of over complexity.

If parameters to a function go over 80 characters width, align with single spaces.
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
if( 8 == max ), looks clearer than if(8 == max)
```
For functions that return booleans, use no spaces.
```
if(main.TXT.equals(".txt"))
```

Use left-hand comparisons because if '=' is typed instead of '==',
```
if( 2 = a ); /* Throws a compile-time error. */
if( a = 2 ); /* Sets a to 2. */
```

For conditions that go over 80 characters width, align using one space and the end ')' one after
any '&&' or '||'s.
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

It also looks a bit clearer, for example
```
/*If the activity is running, make a toast notification. Log the event regardless. */
if( main.service_handler != null )
   Toast.makeText(main.activity_context, (CharSequence) message, Toast.LENGTH_LONG).show();

/*This function could be called from a state where it is impossible to get storage so this is optional. */
if ( main.storage != null )
   log(main.storage, message);
else if( service_update.storage != null )
   log(service_update.storage, message);
```

vs
```
/*If the activity is running, make a toast notification. Log the event regardless. */
if( main.service_handler != null )
{
   Toast.makeText(main.activity_context, (CharSequence) message, Toast.LENGTH_LONG).show();
}

/*This function could be called from a state where it is impossible to get storage so this is optional. */
if ( main.storage != null )
{
   log(main.storage, message);
}
else if( service_update.storage != null )
{
   log(service_update.storage, message);
}
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
