enablePlugins(JavaServerAppPackaging)
enablePlugins(SystemVPlugin)

maintainer := "Olivier Bruchez <olivier@bruchez.org>"

name in Debian := "olivier-bruchez-name"
normalizedName := "olivier-bruchez-name"

packageSummary := "olivier.bruchez.name website"
packageDescription := "olivier.bruchez.name website"

import DebianConstants._

maintainerScripts in Debian := maintainerScriptsAppendFromFile((maintainerScripts in Debian).value)(
  Postinst -> baseDirectory.value / "debian" / "postinst.append"
)
