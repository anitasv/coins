package me.asv.coins;

/**
 * Created with IntelliJ IDEA.
 * User: anita
 * Date: 10/31/13
 * Time: 2:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class Escaper {

    private final String js = "<div id=\"im_1_clickTarget\"></div><script type=\"text/javascript\">(function() {var e=encodeURIComponent,h=\"$BEACON_URL1\",i=\"im_1_clickTarget\",j=\"&\",k=\"0\",l=\"2\",m=\"=\",n=\"?m=\",o=\"error\",p=\"height\",q=\"img\",r=\"src\",s=\"width\";window[\"im_1_recordEvent\"]=function(t,c){function f(a,d){var c=document.getElementById(i),b=document.createElement(q);b.setAttribute(r,a);b.setAttribute(p,k);b.setAttribute(s,l);void 0!=b.addEventListener&&b.addEventListener(o,function(){window.setInterval(function(){3E5<d&&(d=3E5);f(a,2*d)},d)},!1);c.appendChild(b)}var a;a=h+(n+t);for(var g in c)a+=j+e(g)+m+e(c[g]);f(a,1E3)};})();</script>";

}