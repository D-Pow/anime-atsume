(window.webpackJsonp=window.webpackJsonp||[]).push([[1],{602:function(e,t,n){"use strict";n.d(t,"i",(function(){return o})),n.d(t,"e",(function(){return s})),n.d(t,"f",(function(){return l})),n.d(t,"b",(function(){return i})),n.d(t,"a",(function(){return u})),n.d(t,"c",(function(){return m})),n.d(t,"h",(function(){return f})),n.d(t,"d",(function(){return d})),n.d(t,"g",(function(){return p}));var r=n(148),a=n.n(r);function c(e,t){return e+encodeURIComponent(t)}var o="https://kitsu.io/api/edge/anime?filter[text]=",s=function(e){return c(o,e)},l=function(e){return c("https://myanimelist.net/anime.php?q=",e)},i="/searchKissanime",u="/getVideosForEpisode",m=function(e){return c("/corsProxy?url=",e)},f=function(e,t,n,r){return"".concat("/video","/").concat(e,"/").concat(t,"/").concat(n,"?url=").concat(r)},d=function(e){return"".concat("/image","/").concat(e)};function p(e){var t=new URL(e).pathname.split("/"),n=a()(t,4);return{showName:n[2],episodeName:n[3]}}},603:function(e,t,n){"use strict";n.d(t,"e",(function(){return g})),n.d(t,"c",(function(){return E})),n.d(t,"f",(function(){return y})),n.d(t,"b",(function(){return w})),n.d(t,"d",(function(){return j})),n.d(t,"a",(function(){return N}));var r=n(599),a=n.n(r),c=n(109),o=n.n(c),s=n(148),l=n.n(s),i=n(598),u=n.n(i),m=n(2),f=n(606);function d(){return a()(new URLSearchParams(window.location.search).entries()).reduce((function(e,t){var n=l()(t,2),r=n[0],a=n[1];return e[r]=a,e}),{})}function p(e,t){var n=window.location,r=n.origin,c=n.pathname,o=n.hash,s=d();u()(e)===u()({})?s=e:u()(e)===u()("")&&(t?s[e]=t:delete s[e]);var i=r+c+function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:d();return Object.keys(e).length>0?"?".concat(a()(Object.entries(e)).map((function(e){var t=l()(e,2),n=t[0],r=t[1];return"".concat(encodeURIComponent(n),"=").concat(encodeURIComponent(r))})).join("&")):""}(s)+o;history.pushState(null,null,i)}function h(e,t){var n=!(arguments.length>2&&void 0!==arguments[2])||arguments[2],r=function(e,t){return e instanceof Object&&t instanceof Object},a=u()(h),c=function(e,t){return u()(e)===a&&u()(t)===a},o=function(e,t){return Array.isArray(e)&&Array.isArray(t)},s=[];function l(e,t,n){return u()(e)!==u()(t)&&(s.push(n),!0)}function i(e,t,n){return!r(e,t)&&(e!==t&&s.push(n),!0)}function m(e,t,n){return!!c(e,t)&&(e.toString()!==t.toString()&&s.push(n),!0)}function f(e,t,r){if(o(e,t)){for(var a=0;a<e.length||a<t.length;a++){var c="[".concat(a,"]"),s="".concat(r)+(n?c:"");"."===r&&(s=c),p(e[a],t[a],s)}return!0}return!1}function d(e,t,n){var r="."===n?"":"".concat(n,".");return new Set(Object.keys(e).concat(Object.keys(t))).forEach((function(n){p(e[n],t[n],"".concat(r).concat(n))})),!0}function p(e,t,n){return l(e,t,n)||i(e,t,n)||m(e,t,n)||f(e,t,n)||d(e,t,n)}return p(e,t,"."),new Set(s)}function v(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function b(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?v(Object(n),!0).forEach((function(t){o()(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):v(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}function g(e){var t=arguments.length>1&&void 0!==arguments[1]?arguments[1]:{},n=t.initialValue,r=void 0===n?null:n,a=t.type,c=void 0===a?"local":a,o=window["".concat(c,"Storage")],s=u()((function(){})),i=Object(m.useState)((function(){var t=o.getItem(e);return t?JSON.parse(t):r})),f=l()(i,2),d=f[0],p=f[1],h=function(t){var n=t;try{u()(t)===s&&(n=t(d)),p(n),o.setItem(e,JSON.stringify(n))}catch(e){console.error("Could not store value (".concat(t,") to ").concat(c,"Storage. Error ="),e)}};return[d,h]}function E(){var e=u()((function(){})),t=Object(m.useState)(d()),n=l()(t,2),r=n[0],a=n[1],c=function(t,n){if(u()(t)===u()({})){var c=b({},t);return a(c),void p(c)}var o=b({},r),s=n;u()(n)===e&&(s=n(r[t])),o[t]=s,a(o),p(t,s)};return Object(m.useEffect)((function(){var e=d();0!==h(r,e).size&&c(e)}),[window.location.search]),[r,c]}function y(e){var t=arguments.length>1&&void 0!==arguments[1]?arguments[1]:{},n=t.nestedEventField,r=void 0===n?null:n,c=t.initialEventState,o=void 0===c?null:c,s=t.handleEvent,i=void 0===s?null:s,f=t.useEffectInputs,d=void 0===f?[]:f,p=Object(m.useState)(o),h=l()(p,2),v=h[0],b=h[1],g=u()(i)===u()((function(){}));function E(e){var t=r?e[r]:e;g?i(v,b,t):b(t)}return Object(m.useEffect)((function(){return window.addEventListener(e,E),function(){window.removeEventListener(e,E)}}),[e].concat(a()(d))),[v,b]}function w(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:"down";return y("key".concat(e),{nestedEventField:"key"})}function O(){var e=y("click"),t=l()(e,2),n=t[0],r=t[1];return[Object(f.c)(n),r]}function j(e,t){var n=!(arguments.length>2&&void 0!==arguments[2])||arguments[2],r=w(),a=l()(r,2),c=a[0],o=a[1],s=O(),i=l()(s,2),u=i[0],m=i[1],d=n&&"Escape"===c,p=Object(f.b)(e,u),h=Object(f.b)(t,u),v=d||h&&!p,b=function(){o(null),m([])};return[v,b]}function N(e){Object(m.useEffect)((function(){e()?T(!1):T()}),[e])}function T(){var e=!(arguments.length>0&&void 0!==arguments[0])||arguments[0];document.body.style.overflow=e?"auto":"hidden"}},604:function(e,t,n){"use strict";var r=n(86),a=n.n(r),c=n(599),o=n.n(c),s=n(598),l=n.n(s),i=n(2),u=n.n(i),m=n(37),f=n.n(m);function d(e){var t=[e.className],n=[];return e.underlineText&&t.push("underline"),l()(e.rel)===l()("")?n.push(e.rel):l()(e.rel)===l()([])&&n.push.apply(n,o()(e.rel)),u.a.createElement("a",a()({className:t.join(" "),href:e.href,target:e.target,rel:n.join(" ")},e.aria),e.children)}d.Targets={NEW_TAB:"_blank",SAME_TAB:"_self",PARENT:"_parent",TOP:"_top"},d.propTypes={className:f.a.string,href:f.a.string,children:f.a.node,underlineText:f.a.bool,rel:f.a.oneOfType([f.a.string,f.a.arrayOf(f.a.string)]),target:f.a.string,aria:f.a.object},d.defaultProps={className:"",href:"",children:"",underlineText:!0,rel:["noopener","noreferrer"],target:d.Targets.NEW_TAB,aria:{}};var p=d;t.a=p},605:function(e,t,n){"use strict";n.d(t,"a",(function(){return l}));var r=n(596),a=n.n(r),c=n(597),o=n.n(c),s=n(602);function l(e){return i.apply(this,arguments)}function i(){return(i=o()(a.a.mark((function e(t){var n,r;return a.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return e.prev=0,e.next=3,fetch(Object(s.e)(t));case 3:return n=e.sent,e.next=6,n.json();case 6:return e.abrupt("return",e.sent);case 9:return e.prev=9,e.t0=e.catch(0),console.log("Error in fetching Kitsu results: "+e.t0),console.log("Attempting to get them through CORS proxy."),e.next=15,fetch(Object(s.c)(s.i+t));case 15:return r=e.sent,e.next=18,r.json();case 18:return e.abrupt("return",e.sent);case 19:case"end":return e.stop()}}),e,null,[[0,9]])})))).apply(this,arguments)}},606:function(e,t,n){"use strict";n.d(t,"a",(function(){return r})),n.d(t,"c",(function(){return a})),n.d(t,"b",(function(){return c}));n(596),n(597);function r(e,t){var n,r,a=this,c=arguments.length>2&&void 0!==arguments[2]?arguments[2]:{},o=c.callOnFirstFuncCall,s=void 0!==o&&o,l=c.bindThis,i=void 0!==l&&l;return i&&(r=this),function(){for(var c=arguments.length,o=new Array(c),l=0;l<c;l++)o[l]=arguments[l];i||(r=a);var u=s&&null==n;clearTimeout(n),n=setTimeout((function(){n=null,u||e.call.apply(e,[r].concat(o))}),t),u&&e.call.apply(e,[r].concat(o))}}function a(e){if(!e||Array.isArray(e)&&0===e.length)return[];if(e.path)return e.path;for(var t=[],n=e.target;n;)t.push(n),n=n.parentElement;return t.push(document,window),t}function c(e,t){var n=e.attribute,r=e.value,a=!1,c=!0,o=!1,s=void 0;try{for(var l,i=t[Symbol.iterator]();!(c=(l=i.next()).done);c=!0){var u=l.value;if(u instanceof HTMLElement){var m=u.getAttribute(n);if(m&&m.includes(r)){a=!0;break}}}}catch(e){o=!0,s=e}finally{try{c||null==i.return||i.return()}finally{if(o)throw s}}return a}},607:function(e,t,n){var r={"./apple-touch-icon.png":[608,4],"./favicon-144.png":[609,5],"./favicon-192.png":[610,6],"./favicon.ico":[611,7],"./favicon.png":[612,8],"./fonts/BrushScript.eot":[613,9],"./fonts/BrushScript.ttf":[614,10],"./fonts/BrushScript.woff":[615,11],"./react_logo.svg":[616,12]};function a(e){if(!n.o(r,e))return Promise.resolve().then((function(){var t=new Error("Cannot find module '"+e+"'");throw t.code="MODULE_NOT_FOUND",t}));var t=r[e],a=t[0];return n.e(t[1]).then((function(){return n.t(a,7)}))}a.keys=function(){return Object.keys(r)},a.id=607,e.exports=a},617:function(e,t,n){"use strict";n.r(t);var r=n(596),a=n.n(r),c=n(86),o=n.n(c),s=n(109),l=n.n(s),i=n(597),u=n.n(i),m=n(148),f=n.n(m),d=n(2),p=n.n(d),h=n(37),v=n.n(h),b=n(605),g=n(602);function E(e){return y.apply(this,arguments)}function y(){return(y=u()(a.a.mark((function e(t){return a.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return e.next=2,fetch(g.b,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({title:t})}).then((function(e){return e.json()}));case 2:return e.abrupt("return",e.sent);case 3:case"end":return e.stop()}}),e)})))).apply(this,arguments)}var w=n(603),O=n(606),j=n(150),N=n(149),T=n(599),S=n.n(T),x=n(598),k=n.n(x);function P(e){var t=e.className,n=e.title,r=e.children,a=e.footer,c=e.escapeClosesModal,o=e.useGridForBody,s=e.useGridForFooter,l=e.preventDocumentScrolling,i=e.show,u=e.showCloseButton,m=e.onClose,h=e.forwardRef,v=Object(d.useState)(!1),b=f()(v,2),g=b[0],E=b[1],y=Object(w.d)({attribute:"class",value:"modal-content"},{attribute:"class",value:"modal fade"},c),O=f()(y,2),N=O[0],T=O[1],S=function(){E(!0),setTimeout((function(){m(),E(!1)}),500)};N&&(T(),i&&S()),Object(w.a)((function(){return i&&l}));var x=i&&!g?"show":"",P=i?"":"0%",C=k()(n)===k()("")?p.a.createElement("h4",{className:"margin-clear"},n):n;return p.a.createElement("div",{className:"modal fade d-block ".concat(x),style:{background:"rgba(0, 0, 0, 0.7)",width:P,height:P}},p.a.createElement("div",{className:"modal-dialog modal-dialog-centered width-fit m-auto",style:{maxWidth:"90vw"}},p.a.createElement("div",{className:"modal-content overflow-auto "+t,style:{maxHeight:"90vh"},ref:h},p.a.createElement("div",{className:"modal-header",style:Object(j.b)()?{display:"-webkit-box"}:{}},p.a.createElement("div",{className:"modal-title"},C),u&&p.a.createElement("button",{className:"close",onClick:S},p.a.createElement("span",null,"×"))),p.a.createElement("div",{className:"modal-body"},p.a.createElement("div",{className:o?"container-fluid":""},r)),a&&p.a.createElement("div",{className:"modal-footer"},p.a.createElement("div",{className:s?"container-fluid":""},a)))))}P.propTypes={className:v.a.string,title:v.a.node,children:v.a.node,footer:v.a.node,escapeClosesModal:v.a.bool,useGridForBody:v.a.bool,useGridForFooter:v.a.bool,preventDocumentScrolling:v.a.bool,show:v.a.bool,showCloseButton:v.a.bool,onClose:v.a.func,forwardRef:v.a.object},P.defaultProps={className:"",title:"",children:"",footer:"",escapeClosesModal:!0,useGridForBody:!0,useGridForFooter:!0,preventDocumentScrolling:!0,show:!1,showCloseButton:!0,onClose:function(){}};var C=P;function A(e){var t=Object(d.useState)(5),n=f()(t,2),r=n[0],a=n[1],c=Object(w.f)("keydown"),s=f()(c,2),l=s[0],i=s[1],u=Object(d.useRef)(null);return l&&function(){var e=u.current;if(e){switch(l.key){case"ArrowLeft":e.currentTime-=r;break;case"ArrowRight":e.currentTime+=r;break;case"ArrowUp":l.shiftKey?a(r+1):e.volume<=.95?e.volume+=.05:e.volume=1;break;case"ArrowDown":l.shiftKey?a(r-1):e.volume>=.05?e.volume-=.05:e.volume=0;break;case"f":e.requestFullscreen();break;case" ":e.paused?e.play():e.pause()}i(null)}}(),Object(d.useEffect)((function(){u.current&&(u.current.onfocus=function(){return u.current.blur()})}),[u]),p.a.createElement("video",o()({className:e.className,controls:!0,autoPlay:!0,ref:u},e.videoElementProps),p.a.createElement("source",{src:e.src,type:e.type}))}A.propTypes={className:v.a.string,src:v.a.string,type:v.a.string,videoElementProps:v.a.object},A.defaultProps={className:"",src:"",type:"video/mp4",videoElementProps:{}};var F=A;function I(e){return e.show?p.a.createElement("div",{className:"".concat(e.className," ").concat(e.fullScreen?"full-screen-minus-scrollbar":"")},p.a.createElement("div",{className:e.fullScreen?"absolute-center":""},p.a.createElement("h3",{className:"mr-1"},"Sorry, something went wrong."),p.a.createElement("h3",null,e.suggestion))):""}I.propTypes={className:v.a.string,fullScreen:v.a.bool,show:v.a.bool,suggestion:v.a.node},I.defaultProps={className:"",fullScreen:!1,show:!1,suggestion:"Try refreshing the page."};var D=I,U=n(604);function R(e){return L.apply(this,arguments)}function L(){return(L=u()(a.a.mark((function e(t){var n,r=arguments;return a.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return n=r.length>1&&void 0!==r[1]?r[1]:null,e.next=3,fetch(g.a,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({episodeUrl:t,captchaAnswers:n})}).then((function(e){return e.json()}));case 3:return e.abrupt("return",e.sent);case 4:case"end":return e.stop()}}),e)})))).apply(this,arguments)}function B(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function H(e){var t={hasError:!1,showSpinner:!1,captchaPrompts:[],captchaOptions:[],captchaAnswers:[],videoOptions:[],captchaImagesLoaded:new Set,videoHostUrl:null},n=Object(d.useState)(t.hasError),r=f()(n,2),c=r[0],o=r[1],s=Object(d.useState)(t.showSpinner),i=f()(s,2),m=i[0],h=i[1],v=Object(d.useState)(t.captchaPrompts),b=f()(v,2),E=b[0],y=b[1],w=Object(d.useState)(t.captchaOptions),O=f()(w,2),j=O[0],T=O[1],x=Object(d.useState)(t.captchaAnswers),k=f()(x,2),P=k[0],A=k[1],I=Object(d.useState)(t.videoOptions),L=f()(I,2),H=L[0],_=L[1],M=Object(d.useState)(t.captchaImagesLoaded),G=f()(M,2),J=G[0],K=G[1],V=Object(d.useState)(t.videoHostUrl),W=f()(V,2),z=W[0],q=W[1],Y=Object(d.useRef)(null),Q=H.length>0,X=function(){o(t.hasError),y(t.captchaPrompts),T(t.captchaOptions),A(t.captchaAnswers),_(t.videoOptions),K(t.captchaImagesLoaded),q(t.videoHostUrl)};function Z(e,t){return $.apply(this,arguments)}function $(){return($=u()(a.a.mark((function e(t,n){var r,c,s,l;return a.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return h(!0),X(),e.prev=2,e.next=5,R(t,n);case 5:if(!((r=e.sent).status&&r.status>299)){e.next=8;break}throw"Got HTTP status code ".concat(r.status," from server. Error: ").concat(r.error,".");case 8:c=r.data,s=r.captchaContent,l=r.videoHostUrl,s&&(y(s.promptTexts),T(s.imgIdsAndSrcs)),c&&_(c),l&&q(l),h(!1),e.next=19;break;case 15:e.prev=15,e.t0=e.catch(2),console.error("Error fetching for episodes:",e.t0),o(!0);case 19:case"end":return e.stop()}}),e,null,[[2,15]])})))).apply(this,arguments)}Object(d.useEffect)((function(){e.show&&e.episodeUrl&&Z(e.episodeUrl)}),[e.show,e.episodeUrl]);var ee=function(){var t=P.map((function(e,t){var n=j.find((function(t){return t.formId===e})),r=E[t],a=function(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?B(Object(n),!0).forEach((function(t){l()(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):B(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}({},n);return a.promptText=r,a}));Z(e.episodeUrl,t)},te=function(e){return P.includes(e)},ne=function(e){te(e)?A([]):function(e){A((function(t){var n=S()(t);return n.push(e),n}))}(e),Y.current.scrollTo(0,0)};Object(d.useEffect)((function(){e.show&&e.episodeUrl&&P.length>0&&P.length===E.length&&ee()}),[e.show,e.episodeUrl,P.length,E.length]);var re=P.length,ae=E.length?p.a.createElement("div",{className:"text-center"},p.a.createElement("h4",null,"Please solve this captcha"),p.a.createElement("h5",{className:"d-inline"},"(",re+1,"/",E.length,")"),p.a.createElement("h5",{className:"text-danger d-inline ml-1"},E[re])):e.episodeTitle,ce="";return e.show&&e.episodeUrl&&(c?ce=p.a.createElement(D,{suggestion:"Try closing and re-opening the modal.",show:c}):m?ce=p.a.createElement(N.a,{className:"w-40 h-40",show:!0}):E.length?ce=p.a.createElement(p.a.Fragment,null,j.map((function(e,t){var n=e.formId,r=e.imageId;return p.a.createElement(p.a.Fragment,{key:n},p.a.createElement("img",{className:te(n)?"border border-medium border-primary rounded":"",src:Object(g.d)(r),onClick:function(){return ne(n)},onLoad:function(){return e=t,void K((function(t){var n=new Set(t);return n.add(e),n}));var e},alt:"failed to load. sorry"}),t%2==0?"":p.a.createElement(p.a.Fragment,null,p.a.createElement("br",null),p.a.createElement("br",null)),p.a.createElement(N.a,{show:!J.has(t)}))}))):Q?ce=function(){if(Q){var t=Object(g.g)(e.episodeUrl),n=t.showName,r=t.episodeName,a=H[0],c=a.label,o=a.file;return p.a.createElement(F,{className:"w-100",src:Object(g.h)(n,r,c,o),videoElementProps:e.videoElementProps})}}():null!=z&&(ce=p.a.createElement(D,{suggestion:p.a.createElement(p.a.Fragment,null,p.a.createElement("div",null,"The scraper for this host has not been made yet."),p.a.createElement("div",null,"You may watch the video at:"),p.a.createElement(U.a,{className:"d-block",href:z},z)),show:null!=z}))),p.a.createElement(C,{className:m?"overflow-hidden":"",escapeClosesModal:!Q,show:e.show,title:ae,onClose:function(){X(),e.onClose()},forwardRef:Y},p.a.createElement("div",{className:m?"d-flex justify-content-center align-items-center":"overflow-auto",style:{minHeight:"200px"}},ce))}H.propTypes={episodeTitle:v.a.string,episodeUrl:v.a.string,show:v.a.bool,onClose:v.a.func,videoElementProps:v.a.object},H.defaultProps={episodeTitle:"",episodeUrl:null,show:!1,onClose:function(){},videoElementProps:{}};var _=H;function M(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function G(e){var t=decodeURIComponent(e.title),n=Object(d.useState)(!1),r=f()(n,2),c=r[0],s=r[1],i=Object(d.useState)(null),m=f()(i,2),h=m[0],v=m[1],y=Object(d.useState)(null),T=f()(y,2),S=T[0],x=T[1],k=Object(d.useState)(0),P=f()(k,2),C=P[0],A=P[1],F=Object(d.useState)(null),I=f()(F,2),R=I[0],L=I[1],B=Object(d.useState)(null),H=f()(B,2),G=H[0],J=H[1],K=Object(w.e)("showsProgress",{initialValue:{}}),V=f()(K,2),W=V[0],z=V[1];function q(){return(q=u()(a.a.mark((function e(){var n,r,c;return a.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return e.next=2,Object(b.a)(t.toLowerCase());case 2:n=e.sent,r=n.data,c=r.find((function(e){return e.attributes.canonicalTitle===t})),v(c);case 6:case"end":return e.stop()}}),e)})))).apply(this,arguments)}function Y(){return(Y=u()(a.a.mark((function e(){var n;return a.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return e.prev=0,e.next=3,E(t);case 3:if(!((n=e.sent).status&&n.status>299)){e.next=6;break}throw"Got HTTP status code ".concat(n.status," from server. Error: ").concat(n.error,".");case 6:null!=n.results&&n.results.length>=0&&n.results.forEach((function(e){e.episodes.reverse()})),x(n),e.next=14;break;case 10:e.prev=10,e.t0=e.catch(0),console.error("Error fetching for show matches:",e.t0),s(!0);case 14:case"end":return e.stop()}}),e,null,[[0,10]])})))).apply(this,arguments)}Object(d.useEffect)((function(){!function(){q.apply(this,arguments)}(),function(){Y.apply(this,arguments)}()}),[]);var Q=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:R;return S.results[e].title},X=function(e,t){return"".concat(e,"-").concat(t)};var Z=Object(O.a)((function(e){document.getElementById(e).scrollIntoView({block:"center",inline:"center"})}),500),$=Object(O.a)((function(e){var t=document.getElementById(e);t.parentElement.scrollTop=t.offsetTop-t.offsetHeight}),500);if(c)return p.a.createElement(D,{fullScreen:!0,show:c});if(!h||!S)return p.a.createElement(N.a,{fullScreen:!0,show:!0});var ee=h.attributes,te=ee.canonicalTitle,ne=ee.synopsis,re=ee.episodeCount,ae=ee.showType,ce=ee.posterImage.small,oe=[{tabTitle:"Overview",content:p.a.createElement("div",{className:"row"},p.a.createElement("div",{className:"col-centered col-lg-4 my-3 d-flex"},p.a.createElement("img",{className:"my-auto flex-center",src:ce,alt:te,style:{maxWidth:"95%"}})),p.a.createElement("div",{className:"col-sm-12 col-lg-8 d-flex justify-content-center"},p.a.createElement("div",{className:"text-center"},p.a.createElement("div",{className:"row mb-3"},p.a.createElement("div",{className:"col"},p.a.createElement("h5",{className:"capitalize-first"},1===re?ae:re+" episodes"))),p.a.createElement("div",{className:"row mb-3"},p.a.createElement("div",{className:"col"},p.a.createElement("p",null,ne))),p.a.createElement("div",{className:"row mb-3"},p.a.createElement("div",{className:"col"},p.a.createElement(U.a,{href:Object(g.f)(t)},p.a.createElement("h5",null,"View on MyAnimeList")))))))},{tabTitle:"Watch",content:function(){if(null==S.results||0===S.results.length)return p.a.createElement("div",{className:"row d-flex justify-content-center"},p.a.createElement("h4",null,"Sorry, no episodes were found for this show."));var e=S.results.map((function(e,t){var n=e.title,r=e.episodes,a=p.a.createElement("h4",null,p.a.createElement("span",{className:"ml-1 badge badge-pill badge-".concat(R===t?"dark":"primary")},r.length)),c=Object(j.b)()?"div":"button";return p.a.createElement(c,{className:"btn list-group-item remove-focus-highlight ".concat(R===t?"active":""),id:X(t,n),key:t,onClick:function(){return L(t)}},p.a.createElement("div",{className:"d-flex justify-content-between align-items-center"},p.a.createElement("h5",{className:"mb-2 d-flex d-sm-none"},n),p.a.createElement("h3",{className:"mb-2 d-none d-sm-flex"},n),a))})),t=function(){if(null!=R)return S.results[R].episodes.map((function(e,t){var n=e.title,r=e.url,a=W[Q()]===n;return p.a.createElement("a",{className:"list-group-item cursor-pointer ".concat(a?"active":"text-primary"),id:X(R,n),key:t,onClick:function(){return J({episodeTitle:n,episodeUrl:r})}},n)}))}(),n=e&&e.length?"border-top border-bottom":"",r=t&&t.length?"border-top border-bottom":"";return p.a.createElement("div",{className:"row"},p.a.createElement("div",{className:"col-sm-12 col-md-6 mb-5"},p.a.createElement("div",null,p.a.createElement("h3",{className:"mb-2 d-none d-sm-inline-block"},"Shows"),p.a.createElement("h4",{className:"mb-2 d-inline-block d-sm-none"},"Shows"),p.a.createElement("h4",{className:"d-inline-block ml-1"},"(# episodes)")),p.a.createElement("div",{className:"text-left list-group overflow-auto ".concat(n," fix-strange-z-index-scrollbars scroll-auto"),style:{maxHeight:"400px"}},e)),p.a.createElement("div",{className:"col-sm-12 col-md-6"},p.a.createElement("div",null,p.a.createElement("h3",{className:"mb-2 d-none d-sm-block"},"Episodes"),p.a.createElement("h4",{className:"mb-2 d-block d-sm-none"},"Episodes")),p.a.createElement("div",{className:"text-left list-group overflow-auto ".concat(r," fix-strange-z-index-scrollbars"),style:{maxHeight:"400px"}},t)))}()}],se=p.a.createElement("nav",null,p.a.createElement("ul",{className:"pagination"},oe.map((function(e,t){var n=e.tabTitle;return p.a.createElement("li",{className:"page-item ".concat(C===t?"active":""),key:t,onClick:function(){return A(t)},style:{width:"".concat(100/oe.length,"%")}},p.a.createElement("a",{className:"page-link cursor-pointer"},n))}))));return p.a.createElement(p.a.Fragment,null,p.a.createElement("div",{className:"row pb-4"},p.a.createElement("h1",{className:"text-center mx-auto mt-5"},t)),function(){if(!S||1!==C)return null;var e=(S.results?S.results.map((function(e,t){var n=e.title;return{episodeTitle:W[n],showTitle:n,showIndex:t}})).filter((function(e){return null!=e.episodeTitle})):[]).map((function(e){var t=e.showTitle,n=e.showIndex,r=e.episodeTitle;return p.a.createElement("div",{className:"row mb-1",key:n},p.a.createElement("div",{className:"col-12"},p.a.createElement("span",{className:"h5"},p.a.createElement("span",{className:"underline"},t),":"),p.a.createElement("button",{className:"btn btn-link remove-focus-highlight border-0",onClick:function(){return function(e,t){var n=X(e,Q(e)),r=X(e,t);L(e),$(n),Z(r)}(n,r)}},p.a.createElement("h5",{className:"m-0"},r))))}));return e.length?p.a.createElement("div",{className:"row pb-2"},p.a.createElement("div",{className:"col-12"},p.a.createElement("h4",{className:"mb-2"},"Last watched episodes:"),e)):void 0}(),p.a.createElement("div",{className:"row pt-5"},p.a.createElement("div",{className:"col-12"},p.a.createElement("div",{className:"card mb-5"},se,p.a.createElement("div",{className:"card-body"},oe[C].content)))),p.a.createElement(_,o()({},G,{show:null!=G,onClose:function(){return J(null)},videoElementProps:{onLoadStart:function(){var e=Q(),t=G.episodeTitle;z((function(n){var r=function(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?M(Object(n),!0).forEach((function(t){l()(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):M(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}({},n);return r[e]=t,r}))}}})))}G.propTypes={title:v.a.string},G.defaultProps={title:""};var J=G;t.default=J},618:function(e,t,n){"use strict";n.r(t);var r=n(596),a=n.n(r),c=n(597),o=n.n(c),s=n(148),l=n.n(s),i=n(2),u=n.n(i),m=n(605),f=n(37),d=n.n(f),p=n(603);function h(e){var t=Object(p.b)(),n=l()(t,2),r=n[0],a=n[1];"Enter"===r&&(e.handleSubmit(),a(null));var c=u.a.createElement("span",null,u.a.createElement("i",{className:"fas fa-search"})),o=e.btnDisplay?e.btnDisplay:c;return u.a.createElement("div",{className:"row mt-3 mb-5"},u.a.createElement("div",{className:"col-12 col-md-6 mx-auto"},u.a.createElement("div",{className:"input-group my-3"},u.a.createElement("input",{className:"form-control input-large remove-focus-highlight",type:"text",placeholder:'e.g. "Kimi no na wa"',value:e.value,onChange:function(t){var n=t.target.value;e.handleTyping(n)}}),u.a.createElement("div",{className:"input-group-append"},u.a.createElement("button",{className:"btn btn-outline-secondary remove-focus-highlight",onClick:function(){return e.handleSubmit()}},o)))))}h.propTypes={btnDisplay:d.a.node,value:d.a.string,handleTyping:d.a.func,handleSubmit:d.a.func},h.defaultProps={btnDisplay:null,value:"",handleTyping:function(){},handleSubmit:function(){}};var v=h,b=n(109),g=n.n(b),E=n(86),y=n.n(E),w=n(600),O=n.n(w),j=n(604),N=n(602);function T(e){var t=e.anchorImageFunc,n=e.anchorImageTarget,r=e.anchorTitleFunc,a=e.anchorTitleTarget,c=e.kitsuResult;if(!c||!c.attributes)return"";var o=c.attributes,s=o.canonicalTitle,l=o.synopsis,i=o.episodeCount,m=o.showType,f=o.posterImage.small;return u.a.createElement(u.a.Fragment,null,u.a.createElement("div",{className:"col-sm-12 col-md-6"},u.a.createElement(j.a,{className:"m-auto",target:n,href:t(s)},u.a.createElement("img",{className:"align-self-center img-thumbnail",src:f,alt:s}))),u.a.createElement("div",{className:"media-body align-self-center ml-2 mt-2"},u.a.createElement("h5",null,u.a.createElement(j.a,{target:a,href:r(s)},s)," (".concat(1===i?m:i+" episodes",")")),u.a.createElement("p",null,l)))}T.propTypes={anchorImageFunc:d.a.func,anchorImageTarget:d.a.oneOf(Object.values(j.a.Targets)),anchorTitleFunc:d.a.func,anchorTitleTarget:d.a.oneOf(Object.values(j.a.Targets)),kitsuResult:d.a.object},T.defaultProps={anchorImageFunc:function(e){return Object(N.f)(e)},anchorImageTarget:j.a.Targets.SAME_TAB,anchorTitleFunc:function(e){return Object(N.f)(e)},anchorTitleTarget:j.a.Targets.SAME_TAB};var S=T;function x(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);t&&(r=r.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,r)}return n}function k(e){var t=e.kitsuResults,n=O()(e,["kitsuResults"]);return t?u.a.createElement("div",{className:"row my-5"},u.a.createElement("div",{className:"col-12 mx-auto"},u.a.createElement("ul",{className:"list-unstyled"},t.data.map((function(e){return u.a.createElement("li",{className:"media row w-75 mb-5 mx-auto d-flex align-items-center justify-content-center",key:e.id},u.a.createElement(S,y()({},n,{kitsuResult:e})))}))))):""}k.propTypes=function(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?x(Object(n),!0).forEach((function(t){g()(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):x(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}({},S.propTypes,{kitsuResults:d.a.object});var P=k,C=n(149);var A=function(){var e="Anime Atsume",t="Search aggregator for many anime shows.",n=Object(i.useState)(""),r=l()(n,2),c=r[0],s=r[1],f=Object(i.useState)(null),d=l()(f,2),h=d[0],b=d[1],g=Object(i.useState)(!1),E=l()(g,2),y=E[0],w=E[1],O=Object(p.c)(),j=l()(O,2),N=j[0],T=j[1],S=function(){var e=o()(a.a.mark((function e(t){var n,r,o;return a.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return r=(n=t||c).toLowerCase(),w(!0),T("search",n),e.next=6,Object(m.a)(r);case 6:o=e.sent,b(o),w(!1);case 9:case"end":return e.stop()}}),e)})));return function(t){return e.apply(this,arguments)}}();Object(i.useEffect)((function(){var e=N.search;e&&(s(e),S(e))}),[]);var x=function(e){return"#/show/".concat(encodeURIComponent(e))},k=u.a.createElement("div",{className:"row"},u.a.createElement("div",{className:"col-12 text-center mx-auto mt-5"},u.a.createElement("h1",null,e))),A=u.a.createElement("div",{className:"row mt-3"},u.a.createElement("div",{className:"col-12 col-md-6 text-center mx-auto"},u.a.createElement("h6",null,t))),F=y?u.a.createElement(C.a,{type:C.a.Type.CIRCLE,show:y}):null;return u.a.createElement("div",{className:"text-center mx-auto"},k,A,u.a.createElement(v,{btnDisplay:F,value:c,handleTyping:s,handleSubmit:S}),u.a.createElement(P,{anchorImageFunc:x,anchorTitleFunc:x,kitsuResults:h}))};t.default=A}}]);