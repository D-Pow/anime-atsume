(window.webpackJsonp=window.webpackJsonp||[]).push([[3],{618:function(e,t,a){"use strict";a.r(t);var n=a(596),r=a.n(n),c=a(597),l=a.n(c),o=a(148),s=a.n(o),u=a(2),i=a.n(u),m=a(143),p=a(605),h=a(37),f=a.n(h),d=a(149),b=a(603);function g(e){var t=Object(b.b)(),a=s()(t,2),n=a[0],r=a[1];"Enter"===n&&(e.handleSubmit(),r(null));var c=i.a.createElement("span",null,i.a.createElement("i",{className:"fas fa-search"})),l=i.a.createElement(d.a,{type:d.a.Type.CIRCLE,show:e.showBtnSpinner}),o=e.showBtnSpinner?l:e.btnChildren?e.btnChildren:c;return i.a.createElement("div",{className:"row mt-3 mb-5"},i.a.createElement("div",{className:"col-12 col-md-6 mx-auto"},i.a.createElement("div",{className:"input-group my-3"},i.a.createElement("input",{className:"form-control remove-focus-highlight",type:"text",autoFocus:e.focusOnLoad,placeholder:'e.g. "Kimi no na wa"',value:e.value,onChange:function(t){var a=t.target.value;e.handleTyping(a)}}),i.a.createElement("div",{className:"input-group-append"},i.a.createElement("button",{className:"btn btn-outline-secondary remove-focus-highlight",onClick:function(){return e.handleSubmit()}},o)))))}g.propTypes={btnChildren:f.a.node,focusOnLoad:f.a.bool,value:f.a.string,showBtnSpinner:f.a.bool,handleTyping:f.a.func,handleSubmit:f.a.func},g.defaultProps={btnChildren:null,focusOnLoad:!0,value:"",showBtnSpinner:!1,handleTyping:function(){},handleSubmit:function(){}};var v=g,E=a(109),y=a.n(E),O=a(86),w=a.n(O),T=a(600),j=a.n(T),N=a(604),S=a(602);function x(e){var t=e.anchorImageFunc,a=e.anchorImageTarget,n=e.anchorTitleFunc,r=e.anchorTitleTarget,c=e.kitsuResult;if(!c||!c.attributes)return"";var l=c.attributes,o=l.canonicalTitle,s=l.synopsis,u=l.episodeCount,m=l.showType,p=l.posterImage.small;return i.a.createElement(i.a.Fragment,null,i.a.createElement("div",{className:"col-sm-12 col-md-6"},i.a.createElement(N.a,{className:"m-auto",target:a,href:t(o)},i.a.createElement("img",{className:"align-self-center img-thumbnail",src:p,alt:o}))),i.a.createElement("div",{className:"media-body align-self-center ml-2 mt-2"},i.a.createElement("h5",null,i.a.createElement(N.a,{target:r,href:n(o)},o)," (".concat(1===u?m:u+" episodes",")")),i.a.createElement("p",null,s)))}x.propTypes={anchorImageFunc:f.a.func,anchorImageTarget:f.a.oneOf(Object.values(N.a.Targets)),anchorTitleFunc:f.a.func,anchorTitleTarget:f.a.oneOf(Object.values(N.a.Targets)),kitsuResult:f.a.object},x.defaultProps={anchorImageFunc:function(e){return Object(S.f)(e)},anchorImageTarget:N.a.Targets.SAME_TAB,anchorTitleFunc:function(e){return Object(S.f)(e)},anchorTitleTarget:N.a.Targets.SAME_TAB};var k=x;function C(e,t){var a=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),a.push.apply(a,n)}return a}function F(e){var t=e.kitsuResults,a=j()(e,["kitsuResults"]);return t?i.a.createElement("div",{className:"row my-5"},i.a.createElement("div",{className:"col-12 mx-auto"},i.a.createElement("ul",{className:"list-unstyled"},t.data.map((function(e){return i.a.createElement("li",{className:"media row w-75 mb-5 mx-auto d-flex align-items-center justify-content-center",key:e.id},i.a.createElement(k,w()({},a,{kitsuResult:e})))}))))):""}F.propTypes=function(e){for(var t=1;t<arguments.length;t++){var a=null!=arguments[t]?arguments[t]:{};t%2?C(Object(a),!0).forEach((function(t){y()(e,t,a[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(a)):C(Object(a)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(a,t))}))}return e}({},k.propTypes,{kitsuResults:f.a.object});var I=F;var P=function(){var e="Anime Atsume",t="Search aggregator for many anime shows.",a=Object(u.useState)(""),n=s()(a,2),c=n[0],o=n[1],h=Object(u.useState)(null),f=s()(h,2),d=f[0],g=f[1],E=Object(u.useState)(!1),y=s()(E,2),O=y[0],w=y[1],T=Object(b.c)(),j=s()(T,2),N=j[0],S=j[1],x=function(){var e=l()(r.a.mark((function e(t){var a,n,l;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return n=(a=t||c).toLowerCase(),w(!0),S("search",a),e.next=6,Object(p.a)(n);case 6:l=e.sent,g(l),w(!1);case 9:case"end":return e.stop()}}),e)})));return function(t){return e.apply(this,arguments)}}();Object(u.useEffect)((function(){var e=N.search;e&&(o(e),x(e))}),[]);var k=function(e){return"#/show/".concat(encodeURIComponent(e))},C=i.a.createElement("div",{className:"row"},i.a.createElement("div",{className:"col-12 text-center mx-auto mt-5"},i.a.createElement("h1",null,e))),F=i.a.createElement("div",{className:"row mt-3"},i.a.createElement("div",{className:"col-12 col-md-6 text-center mx-auto"},i.a.createElement("h6",null,t))),P=i.a.createElement("div",{className:"row mt-3"},i.a.createElement("h6",{className:"col-12 col-md-6 text-center mx-auto"},i.a.createElement(m.Link,{className:"underline",to:"/about"},"What is ",e,"?")));return i.a.createElement("div",{className:"text-center mx-auto col-12"},C,F,P,i.a.createElement(v,{value:c,showBtnSpinner:O,handleTyping:o,handleSubmit:x}),i.a.createElement(I,{anchorImageFunc:k,anchorTitleFunc:k,kitsuResults:d}))};t.default=P}}]);