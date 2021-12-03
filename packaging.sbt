enablePlugins(JavaServerAppPackaging)
enablePlugins(SystemVPlugin)

maintainer := "Olivier Bruchez <olivier@bruchez.org>"

Debian / name := "olivier-bruchez-name"
normalizedName := "olivier-bruchez-name"

packageSummary := "olivier.bruchez.name website"
packageDescription := "olivier.bruchez.name website"

import DebianConstants._

Debian / maintainerScripts := maintainerScriptsAppendFromFile((Debian / maintainerScripts).value)(
  Postinst -> baseDirectory.value / "debian" / "postinst.append"
)
