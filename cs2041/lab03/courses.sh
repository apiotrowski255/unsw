ug="http://www.handbook.unsw.edu.au/vbook2017/brCoursesByAtoZ.jsp?StudyLevelUndergraduate&descr=All"
pg="http://www.handbook.unsw.edu.au/vbook2017/brCoursesByAtoZ.jsp?StudyLevelPostgraduate&descr=All"

wget -q -O- $ug $pg |egrep $1 | egrep [A-Z]{4}[0-9]{4} | sed -e 's/^\s*//' -e '/^$/d' | sed -e "s/>/\n/" | sed -e "s/</\n/" | egrep [A-Z]{4}[0-9]{4} | egrep ^A |
cut -c68-200 | tr "\." " " |tr "<>" " " | cut -d" " -f1,3-99 | sed "s/.A //" | sed "s/ .TD//" | sed "s/ .A//"
