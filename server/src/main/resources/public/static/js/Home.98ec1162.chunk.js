(window.webpackJsonp=window.webpackJsonp||[]).push([[1],{601:function(e,t,n){"use strict";n.d(t,"i",(function(){return o})),n.d(t,"e",(function(){return s})),n.d(t,"f",(function(){return l})),n.d(t,"b",(function(){return i})),n.d(t,"a",(function(){return u})),n.d(t,"c",(function(){return m})),n.d(t,"h",(function(){return d})),n.d(t,"d",(function(){return f})),n.d(t,"g",(function(){return p}));var a=n(147),r=n.n(a);function c(e,t){return e+encodeURIComponent(t)}var o="https://kitsu.io/api/edge/anime?filter[text]=",s=function(e){return c(o,e)},l=function(e){return c("https://myanimelist.net/anime.php?q=",e)},i="/searchKissanime",u="/getVideosForEpisode",m=function(e){return c("/corsProxy?url=",e)},d=function(e,t,n,a){return"".concat("/video","/").concat(e,"/").concat(t,"/").concat(n,"?url=").concat(a)},f=function(e){return"".concat("/image","/").concat(e)};function p(e){var t=new URL(e).pathname.split("/"),n=r()(t,4);return{showName:n[2],episodeName:n[3]}}},602:function(e,t,n){"use strict";n.d(t,"d",(function(){return m})),n.d(t,"e",(function(){return d})),n.d(t,"b",(function(){return f})),n.d(t,"c",(function(){return h})),n.d(t,"a",(function(){return v}));n(108);var a=n(597),r=n.n(a),c=n(147),o=n.n(c),s=n(598),l=n.n(s),i=n(2),u=n(605);function m(e){var t=arguments.length>1&&void 0!==arguments[1]?arguments[1]:{},n=t.initialValue,a=void 0===n?null:n,r=t.type,c=void 0===r?"local":r,s=window["".concat(c,"Storage")],u=l()((function(){})),m=Object(i.useState)((function(){var t=s.getItem(e);return t?JSON.parse(t):a})),d=o()(m,2),f=d[0],p=d[1],h=function(t){var n=t;try{l()(t)===u&&(n=t(f)),p(n),s.setItem(e,JSON.stringify(n))}catch(e){console.error("Could not store value (".concat(t,") to ").concat(c,"Storage. Error ="),e)}};return[f,h]}function d(e){var t=arguments.length>1&&void 0!==arguments[1]?arguments[1]:{},n=t.nestedEventField,a=void 0===n?null:n,c=t.initialEventState,s=void 0===c?null:c,u=t.handleEvent,m=void 0===u?null:u,d=t.useEffectInputs,f=void 0===d?[]:d,p=Object(i.useState)(s),h=o()(p,2),v=h[0],b=h[1],g=l()(m)===l()((function(){}));function E(e){var t=a?e[a]:e;g?m(v,b,t):b(t)}return Object(i.useEffect)((function(){return window.addEventListener(e,E),function(){window.removeEventListener(e,E)}}),[e].concat(r()(f))),[v,b]}function f(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:"down";return d("key".concat(e),{nestedEventField:"key"})}function p(){var e=d("click"),t=o()(e,2),n=t[0],a=t[1];return[Object(u.c)(n),a]}function h(e,t){var n=!(arguments.length>2&&void 0!==arguments[2])||arguments[2],a=f(),r=o()(a,2),c=r[0],s=r[1],l=p(),i=o()(l,2),m=i[0],d=i[1],h=n&&"Escape"===c,v=Object(u.b)(e,m),b=Object(u.b)(t,m),g=h||b&&!v,E=function(){s(null),d([])};return[g,E]}function v(e){Object(i.useEffect)((function(){e()?b(!1):b()}),[e])}function b(){var e=!(arguments.length>0&&void 0!==arguments[0])||arguments[0];document.body.style.overflow=e?"auto":"hidden"}},603:function(e,t,n){"use strict";var a=n(85),r=n.n(a),c=n(597),o=n.n(c),s=n(598),l=n.n(s),i=n(2),u=n.n(i),m=n(37),d=n.n(m);function f(e){var t=[e.className],n=[];return e.underlineText&&t.push("underline"),l()(e.rel)===l()("")?n.push(e.rel):l()(e.rel)===l()([])&&n.push.apply(n,o()(e.rel)),u.a.createElement("a",r()({className:t.join(" "),href:e.href,target:e.target,rel:n.join(" ")},e.aria),e.children)}f.Targets={NEW_TAB:"_blank",SAME_TAB:"_self",PARENT:"_parent",TOP:"_top"},f.propTypes={className:d.a.string,href:d.a.string,children:d.a.node,underlineText:d.a.bool,rel:d.a.oneOfType([d.a.string,d.a.arrayOf(d.a.string)]),target:d.a.string,aria:d.a.object},f.defaultProps={className:"",href:"",children:"",underlineText:!0,rel:["noopener","noreferrer"],target:f.Targets.NEW_TAB,aria:{}};var p=f;t.a=p},604:function(e,t,n){"use strict";n.d(t,"a",(function(){return l}));var a=n(595),r=n.n(a),c=n(596),o=n.n(c),s=n(601);function l(e){return i.apply(this,arguments)}function i(){return(i=o()(r.a.mark((function e(t){var n,a;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return e.prev=0,e.next=3,fetch(Object(s.e)(t));case 3:return n=e.sent,e.next=6,n.json();case 6:return e.abrupt("return",e.sent);case 9:return e.prev=9,e.t0=e.catch(0),console.log("Error in fetching Kitsu results: "+e.t0),console.log("Attempting to get them through CORS proxy."),e.next=15,fetch(Object(s.c)(s.i+t));case 15:return a=e.sent,e.next=18,a.json();case 18:return e.abrupt("return",e.sent);case 19:case"end":return e.stop()}}),e,null,[[0,9]])})))).apply(this,arguments)}},605:function(e,t,n){"use strict";n.d(t,"a",(function(){return a})),n.d(t,"c",(function(){return r})),n.d(t,"b",(function(){return c}));n(595),n(596);function a(e,t){var n,a,r=this,c=arguments.length>2&&void 0!==arguments[2]?arguments[2]:{},o=c.callOnFirstFuncCall,s=void 0!==o&&o,l=c.bindThis,i=void 0!==l&&l;return i&&(a=this),function(){for(var c=arguments.length,o=new Array(c),l=0;l<c;l++)o[l]=arguments[l];i||(a=r);var u=s&&null==n;clearTimeout(n),n=setTimeout((function(){n=null,u||e.call.apply(e,[a].concat(o))}),t),u&&e.call.apply(e,[a].concat(o))}}function r(e){if(!e||Array.isArray(e)&&0===e.length)return[];if(e.path)return e.path;for(var t=[],n=e.target;n;)t.push(n),n=n.parentElement;return t.push(document,window),t}function c(e,t){var n=e.attribute,a=e.value,r=!1,c=!0,o=!1,s=void 0;try{for(var l,i=t[Symbol.iterator]();!(c=(l=i.next()).done);c=!0){var u=l.value;if(u instanceof HTMLElement){var m=u.getAttribute(n);if(m&&m.includes(a)){r=!0;break}}}}catch(e){o=!0,s=e}finally{try{c||null==i.return||i.return()}finally{if(o)throw s}}return r}},606:function(e,t,n){var a={"./apple-touch-icon.png":[607,4],"./favicon-144.png":[608,5],"./favicon-192.png":[609,6],"./favicon.ico":[610,7],"./favicon.png":[611,8],"./fonts/BrushScript.eot":[612,9],"./fonts/BrushScript.ttf":[613,10],"./fonts/BrushScript.woff":[614,11],"./react_logo.svg":[615,12]};function r(e){if(!n.o(a,e))return Promise.resolve().then((function(){var t=new Error("Cannot find module '"+e+"'");throw t.code="MODULE_NOT_FOUND",t}));var t=a[e],r=t[0];return n.e(t[1]).then((function(){return n.t(r,7)}))}r.keys=function(){return Object.keys(a)},r.id=606,e.exports=r},616:function(e,t,n){"use strict";n.r(t);var a=n(595),r=n.n(a),c=n(85),o=n.n(c),s=n(108),l=n.n(s),i=n(596),u=n.n(i),m=n(147),d=n.n(m),f=n(2),p=n.n(f),h=n(37),v=n.n(h),b=n(604),g=n(601);function E(e){return y.apply(this,arguments)}function y(){return(y=u()(r.a.mark((function e(t){return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return e.next=2,fetch(g.b,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({title:t})}).then((function(e){return e.json()}));case 2:return e.abrupt("return",e.sent);case 3:case"end":return e.stop()}}),e)})))).apply(this,arguments)}var w=n(602),O=n(605),N=n(149),j=n(148),T=n(597),x=n.n(T),S=n(598),k=n.n(S);function P(e){var t=e.className,n=e.title,a=e.children,r=e.footer,c=e.escapeClosesModal,o=e.useGridForBody,s=e.useGridForFooter,l=e.preventDocumentScrolling,i=e.show,u=e.showCloseButton,m=e.onClose,h=e.forwardRef,v=Object(f.useState)(!1),b=d()(v,2),g=b[0],E=b[1],y=Object(w.c)({attribute:"class",value:"modal-content"},{attribute:"class",value:"modal fade"},c),O=d()(y,2),j=O[0],T=O[1],x=function(){E(!0),setTimeout((function(){m(),E(!1)}),500)};j&&(T(),i&&x()),Object(w.a)((function(){return i&&l}));var S=i&&!g?"show":"",P=i?"":"0%",C=k()(n)===k()("")?p.a.createElement("h4",{className:"margin-clear"},n):n;return p.a.createElement("div",{className:"modal fade d-block ".concat(S),style:{background:"rgba(0, 0, 0, 0.7)",width:P,height:P}},p.a.createElement("div",{className:"modal-dialog modal-dialog-centered width-fit",style:{maxWidth:"90vw"}},p.a.createElement("div",{className:"modal-content overflow-auto "+t,style:{maxHeight:"90vh"},ref:h},p.a.createElement("div",{className:"modal-header",style:Object(N.b)()?{display:"-webkit-box"}:{}},p.a.createElement("div",{className:"modal-title"},C),u&&p.a.createElement("button",{className:"close",onClick:x},p.a.createElement("span",null,"×"))),p.a.createElement("div",{className:"modal-body"},p.a.createElement("div",{className:o?"container-fluid":""},a)),r&&p.a.createElement("div",{className:"modal-footer"},p.a.createElement("div",{className:s?"container-fluid":""},r)))))}P.propTypes={className:v.a.string,title:v.a.node,children:v.a.node,footer:v.a.node,escapeClosesModal:v.a.bool,useGridForBody:v.a.bool,useGridForFooter:v.a.bool,preventDocumentScrolling:v.a.bool,show:v.a.bool,showCloseButton:v.a.bool,onClose:v.a.func,forwardRef:v.a.object},P.defaultProps={className:"",title:"",children:"",footer:"",escapeClosesModal:!0,useGridForBody:!0,useGridForFooter:!0,preventDocumentScrolling:!0,show:!1,showCloseButton:!0,onClose:function(){}};var C=P;function F(e){var t=Object(f.useState)(5),n=d()(t,2),a=n[0],r=n[1],c=Object(w.e)("keydown"),s=d()(c,2),l=s[0],i=s[1],u=Object(f.useRef)(null);return l&&function(){var e=u.current;if(e){switch(l.key){case"ArrowLeft":e.currentTime-=a;break;case"ArrowRight":e.currentTime+=a;break;case"ArrowUp":l.shiftKey?r(a+1):e.volume<=.95?e.volume+=.05:e.volume=1;break;case"ArrowDown":l.shiftKey?r(a-1):e.volume>=.05?e.volume-=.05:e.volume=0;break;case"f":e.requestFullscreen();break;case" ":e.paused?e.play():e.pause()}i(null)}}(),Object(f.useEffect)((function(){u.current&&(u.current.onfocus=function(){return u.current.blur()})}),[u]),p.a.createElement("video",o()({className:e.className,controls:!0,autoPlay:!0,ref:u},e.videoElementProps),p.a.createElement("source",{src:e.src,type:e.type}))}F.propTypes={className:v.a.string,src:v.a.string,type:v.a.string,videoElementProps:v.a.object},F.defaultProps={className:"",src:"",type:"video/mp4",videoElementProps:{}};var I=F;function A(e){return e.show?p.a.createElement("div",{className:"".concat(e.className," ").concat(e.fullScreen?"full-screen-minus-scrollbar":"")},p.a.createElement("div",{className:e.fullScreen?"absolute-center":""},p.a.createElement("h3",{className:"mr-1"},"Sorry, something went wrong."),p.a.createElement("h3",null,e.suggestion))):""}A.propTypes={className:v.a.string,fullScreen:v.a.bool,show:v.a.bool,suggestion:v.a.node},A.defaultProps={className:"",fullScreen:!1,show:!1,suggestion:"Try refreshing the page."};var D=A,U=n(603);function R(e){return B.apply(this,arguments)}function B(){return(B=u()(r.a.mark((function e(t){var n,a=arguments;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return n=a.length>1&&void 0!==a[1]?a[1]:null,e.next=3,fetch(g.a,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({episodeUrl:t,captchaAnswers:n})}).then((function(e){return e.json()}));case 3:return e.abrupt("return",e.sent);case 4:case"end":return e.stop()}}),e)})))).apply(this,arguments)}function L(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function H(e){var t={hasError:!1,showSpinner:!1,captchaPrompts:[],captchaOptions:[],captchaAnswers:[],videoOptions:[],captchaImagesLoaded:new Set,videoHostUrl:null},n=Object(f.useState)(t.hasError),a=d()(n,2),c=a[0],o=a[1],s=Object(f.useState)(t.showSpinner),i=d()(s,2),m=i[0],h=i[1],v=Object(f.useState)(t.captchaPrompts),b=d()(v,2),E=b[0],y=b[1],w=Object(f.useState)(t.captchaOptions),O=d()(w,2),N=O[0],T=O[1],S=Object(f.useState)(t.captchaAnswers),k=d()(S,2),P=k[0],F=k[1],A=Object(f.useState)(t.videoOptions),B=d()(A,2),H=B[0],_=B[1],M=Object(f.useState)(t.captchaImagesLoaded),G=d()(M,2),J=G[0],K=G[1],V=Object(f.useState)(t.videoHostUrl),W=d()(V,2),z=W[0],q=W[1],Y=Object(f.useRef)(null),Q=H.length>0,X=function(){o(t.hasError),y(t.captchaPrompts),T(t.captchaOptions),F(t.captchaAnswers),_(t.videoOptions),K(t.captchaImagesLoaded),q(t.videoHostUrl)};function Z(e,t){return $.apply(this,arguments)}function $(){return($=u()(r.a.mark((function e(t,n){var a,c,s,l;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return h(!0),X(),e.prev=2,e.next=5,R(t,n);case 5:if(!((a=e.sent).status&&a.status>299)){e.next=8;break}throw"Got HTTP status code ".concat(a.status," from server. Error: ").concat(a.error,".");case 8:c=a.data,s=a.captchaContent,l=a.videoHostUrl,s&&(y(s.promptTexts),T(s.imgIdsAndSrcs)),c&&_(c),l&&q(l),h(!1),e.next=19;break;case 15:e.prev=15,e.t0=e.catch(2),console.error("Error fetching for episodes:",e.t0),o(!0);case 19:case"end":return e.stop()}}),e,null,[[2,15]])})))).apply(this,arguments)}Object(f.useEffect)((function(){e.show&&e.episodeUrl&&Z(e.episodeUrl)}),[e.show,e.episodeUrl]);var ee=function(){var t=P.map((function(e,t){var n=N.find((function(t){return t.formId===e})),a=E[t],r=function(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?L(Object(n),!0).forEach((function(t){l()(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):L(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}({},n);return r.promptText=a,r}));Z(e.episodeUrl,t)},te=function(e){return P.includes(e)},ne=function(e){te(e)?F([]):function(e){F((function(t){var n=x()(t);return n.push(e),n}))}(e),Y.current.scrollTo(0,0)};Object(f.useEffect)((function(){e.show&&e.episodeUrl&&P.length>0&&P.length===E.length&&ee()}),[e.show,e.episodeUrl,P.length,E.length]);var ae=P.length,re=E.length?p.a.createElement("div",{className:"text-center"},p.a.createElement("h4",null,"Please solve this captcha"),p.a.createElement("h5",{className:"d-inline"},"(",ae+1,"/",E.length,")"),p.a.createElement("h5",{className:"text-danger d-inline ml-1"},E[ae])):e.episodeTitle,ce="";return e.show&&e.episodeUrl&&(c?ce=p.a.createElement(D,{suggestion:"Try closing and re-opening the modal.",show:c}):m?ce=p.a.createElement(j.a,{className:"w-40 h-40",show:!0}):E.length?ce=p.a.createElement(p.a.Fragment,null,N.map((function(e,t){var n=e.formId,a=e.imageId;return p.a.createElement(p.a.Fragment,{key:n},p.a.createElement("img",{className:te(n)?"border border-medium border-primary rounded":"",src:Object(g.d)(a),onClick:function(){return ne(n)},onLoad:function(){return e=t,void K((function(t){var n=new Set(t);return n.add(e),n}));var e},alt:"failed to load. sorry"}),t%2==0?"":p.a.createElement(p.a.Fragment,null,p.a.createElement("br",null),p.a.createElement("br",null)),p.a.createElement(j.a,{show:!J.has(t)}))}))):Q?ce=function(){if(Q){var t=Object(g.g)(e.episodeUrl),n=t.showName,a=t.episodeName,r=H[0],c=r.label,o=r.file;return p.a.createElement(I,{className:"w-100",src:Object(g.h)(n,a,c,o),videoElementProps:e.videoElementProps})}}():null!=z&&(ce=p.a.createElement(D,{suggestion:p.a.createElement(p.a.Fragment,null,p.a.createElement("div",null,"The scraper for this host has not been made yet."),p.a.createElement("div",null,"You may watch the video at:"),p.a.createElement(U.a,{className:"d-block",href:z},z)),show:null!=z}))),p.a.createElement(C,{className:m?"overflow-hidden":"",escapeClosesModal:!Q,show:e.show,title:re,onClose:function(){X(),e.onClose()},forwardRef:Y},p.a.createElement("div",{className:m?"d-flex justify-content-center align-items-center":"overflow-auto",style:{minHeight:"200px"}},ce))}H.propTypes={episodeTitle:v.a.string,episodeUrl:v.a.string,show:v.a.bool,onClose:v.a.func,videoElementProps:v.a.object},H.defaultProps={episodeTitle:"",episodeUrl:null,show:!1,onClose:function(){},videoElementProps:{}};var _=H;function M(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function G(e){var t=decodeURIComponent(e.title),n=Object(f.useState)(!1),a=d()(n,2),c=a[0],s=a[1],i=Object(f.useState)(null),m=d()(i,2),h=m[0],v=m[1],y=Object(f.useState)(null),T=d()(y,2),x=T[0],S=T[1],k=Object(f.useState)(0),P=d()(k,2),C=P[0],F=P[1],I=Object(f.useState)(null),A=d()(I,2),R=A[0],B=A[1],L=Object(f.useState)(null),H=d()(L,2),G=H[0],J=H[1],K=Object(w.d)("showsProgress",{initialValue:{}}),V=d()(K,2),W=V[0],z=V[1];function q(){return(q=u()(r.a.mark((function e(){var n,a,c;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return e.next=2,Object(b.a)(t.toLowerCase());case 2:n=e.sent,a=n.data,c=a.find((function(e){return e.attributes.canonicalTitle===t})),v(c);case 6:case"end":return e.stop()}}),e)})))).apply(this,arguments)}function Y(){return(Y=u()(r.a.mark((function e(){var n;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return e.prev=0,e.next=3,E(t);case 3:if(!((n=e.sent).status&&n.status>299)){e.next=6;break}throw"Got HTTP status code ".concat(n.status," from server. Error: ").concat(n.error,".");case 6:null!=n.results&&n.results.length>=0&&n.results.forEach((function(e){e.episodes.reverse()})),S(n),e.next=14;break;case 10:e.prev=10,e.t0=e.catch(0),console.error("Error fetching for show matches:",e.t0),s(!0);case 14:case"end":return e.stop()}}),e,null,[[0,10]])})))).apply(this,arguments)}Object(f.useEffect)((function(){!function(){q.apply(this,arguments)}(),function(){Y.apply(this,arguments)}()}),[]);var Q=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:R;return x.results[e].title},X=function(e,t){return"".concat(e,"-").concat(t)};var Z=Object(O.a)((function(e){document.getElementById(e).scrollIntoView({block:"center",inline:"center"})}),500),$=Object(O.a)((function(e){var t=document.getElementById(e);t.parentElement.scrollTop=t.offsetTop-t.offsetHeight}),500);if(c)return p.a.createElement(D,{fullScreen:!0,show:c});if(!h||!x)return p.a.createElement(j.a,{fullScreen:!0,show:!0});var ee=h.attributes,te=ee.canonicalTitle,ne=ee.synopsis,ae=ee.episodeCount,re=ee.showType,ce=ee.posterImage.small,oe=[{tabTitle:"Overview",content:p.a.createElement("div",{className:"row"},p.a.createElement("div",{className:"col-centered col-lg-4 my-3 d-flex"},p.a.createElement("img",{className:"my-auto flex-center",src:ce,alt:te,style:{maxWidth:"95%"}})),p.a.createElement("div",{className:"col-sm-12 col-lg-8 d-flex justify-content-center"},p.a.createElement("div",{className:"text-center"},p.a.createElement("div",{className:"row mb-3"},p.a.createElement("div",{className:"col"},p.a.createElement("h5",{className:"capitalize-first"},1===ae?re:ae+" episodes"))),p.a.createElement("div",{className:"row mb-3"},p.a.createElement("div",{className:"col"},p.a.createElement("p",null,ne))),p.a.createElement("div",{className:"row mb-3"},p.a.createElement("div",{className:"col"},p.a.createElement(U.a,{href:Object(g.f)(t)},p.a.createElement("h5",null,"View on MyAnimeList")))))))},{tabTitle:"Watch",content:null==x.results||0===x.results.length?p.a.createElement("div",{className:"row d-flex justify-content-center"},p.a.createElement("h4",null,"Sorry, no episodes were found for this show.")):p.a.createElement("div",{className:"row"},p.a.createElement("div",{className:"col-6"},p.a.createElement("div",{className:"d-block d-sm-none"},p.a.createElement("h4",{className:"mb-2"},"Shows")),p.a.createElement("div",{className:"d-none d-sm-block"},p.a.createElement("h3",{className:"mb-2 d-inline-block"},"Shows"),p.a.createElement("h4",{className:"d-inline-block ml-1"},"(# episodes)")),p.a.createElement("div",{className:"text-left list-group overflow-auto fix-strange-z-index-scrollbars scroll-auto",style:{maxHeight:"400px"}},x.results.map((function(e,t){var n=e.title,a=e.episodes,r=p.a.createElement("h4",null,p.a.createElement("span",{className:"ml-1 d-none d-sm-block badge badge-pill badge-".concat(R===t?"dark":"primary")},a.length)),c=Object(N.b)()?"div":"button";return p.a.createElement(c,{className:"btn list-group-item remove-focus-highlight ".concat(R===t?"active":""),id:X(t,n),key:t,onClick:function(){return B(t)}},p.a.createElement("div",{className:"d-flex d-sm-none justify-content-between align-items-center"},p.a.createElement("h5",{className:"mb-2"},n),r),p.a.createElement("div",{className:"d-none d-sm-flex justify-content-between align-items-center"},p.a.createElement("h3",{className:"mb-2"},n),r))})))),p.a.createElement("div",{className:"col-6"},p.a.createElement("div",{className:"d-block d-sm-none"},p.a.createElement("h4",{className:"mb-2"},"Episodes")),p.a.createElement("div",{className:"d-none d-sm-block"},p.a.createElement("h3",{className:"mb-2"},"Episodes")),p.a.createElement("div",{className:"text-left list-group overflow-auto fix-strange-z-index-scrollbars",style:{maxHeight:"400px"}},function(){if(null!=R)return x.results[R].episodes.map((function(e,t){var n=e.title,a=e.url,r=W[Q()]===n;return p.a.createElement("a",{className:"list-group-item cursor-pointer ".concat(r?"active":"text-primary"),id:X(R,n),key:t,onClick:function(){return J({episodeTitle:n,episodeUrl:a})}},n)}))}())))}],se=p.a.createElement("nav",null,p.a.createElement("ul",{className:"pagination"},oe.map((function(e,t){var n=e.tabTitle;return p.a.createElement("li",{className:"page-item ".concat(C===t?"active":""),key:t,onClick:function(){return F(t)},style:{width:"".concat(100/oe.length,"%")}},p.a.createElement("a",{className:"page-link cursor-pointer"},n))}))));return p.a.createElement(p.a.Fragment,null,p.a.createElement("div",{className:"row pb-4"},p.a.createElement("h1",{className:"text-center mx-auto mt-5"},t)),function(){if(!x||1!==C)return null;var e=(x.results?x.results.map((function(e,t){var n=e.title;return{episodeTitle:W[n],showTitle:n,showIndex:t}})).filter((function(e){return null!=e.episodeTitle})):[]).map((function(e){var t=e.showTitle,n=e.showIndex,a=e.episodeTitle;return p.a.createElement("div",{className:"row mb-1",key:n},p.a.createElement("div",{className:"col-12"},p.a.createElement("span",{className:"h5"},p.a.createElement("span",{className:"underline"},t),":"),p.a.createElement("button",{className:"btn btn-link remove-focus-highlight border-0",onClick:function(){return function(e,t){var n=X(e,Q(e)),a=X(e,t);B(e),$(n),Z(a)}(n,a)}},p.a.createElement("h5",{className:"m-0"},a))))}));return e.length?p.a.createElement("div",{className:"row pb-2"},p.a.createElement("div",{className:"col-12"},p.a.createElement("h4",{className:"mb-2"},"Last watched episodes:"),e)):void 0}(),p.a.createElement("div",{className:"row pt-5"},p.a.createElement("div",{className:"col-12"},p.a.createElement("div",{className:"card mb-5"},se,p.a.createElement("div",{className:"card-body"},oe[C].content)))),p.a.createElement(_,o()({},G,{show:null!=G,onClose:function(){return J(null)},videoElementProps:{onLoadStart:function(){var e=Q(),t=G.episodeTitle;z((function(n){var a=function(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?M(Object(n),!0).forEach((function(t){l()(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):M(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}({},n);return a[e]=t,a}))}}})))}G.propTypes={title:v.a.string},G.defaultProps={title:""};var J=G;t.default=J},617:function(e,t,n){"use strict";n.r(t);var a=n(595),r=n.n(a),c=n(596),o=n.n(c),s=n(147),l=n.n(s),i=n(2),u=n.n(i),m=n(604),d=n(37),f=n.n(d),p=n(602);function h(e){var t=Object(p.b)(),n=l()(t,2),a=n[0],r=n[1];"Enter"===a&&(e.handleSubmit(),r(null));var c=u.a.createElement("span",null,u.a.createElement("i",{className:"fas fa-search"})),o=e.btnDisplay?e.btnDisplay:c;return u.a.createElement("div",{className:"row mt-3 mb-5"},u.a.createElement("div",{className:"col-12 col-md-6 mx-auto"},u.a.createElement("div",{className:"input-group my-3"},u.a.createElement("input",{className:"form-control input-large remove-focus-highlight",type:"text",placeholder:'e.g. "Kimi no na wa"',value:e.value,onChange:function(t){var n=t.target.value;e.handleTyping(n)}}),u.a.createElement("div",{className:"input-group-append"},u.a.createElement("button",{className:"btn btn-outline-secondary remove-focus-highlight",onClick:function(){return e.handleSubmit()}},o)))))}h.propTypes={btnDisplay:f.a.node,value:f.a.string,handleTyping:f.a.func,handleSubmit:f.a.func},h.defaultProps={btnDisplay:null,value:"",handleTyping:function(){},handleSubmit:function(){}};var v=h,b=n(108),g=n.n(b),E=n(85),y=n.n(E),w=n(599),O=n.n(w),N=n(603),j=n(601);function T(e){var t=e.anchorImageFunc,n=e.anchorImageTarget,a=e.anchorTitleFunc,r=e.anchorTitleTarget,c=e.kitsuResult;if(!c||!c.attributes)return"";var o=c.attributes,s=o.canonicalTitle,l=o.synopsis,i=o.episodeCount,m=o.showType,d=o.posterImage.small;return u.a.createElement(u.a.Fragment,null,u.a.createElement(N.a,{target:n,href:t(s)},u.a.createElement("img",{className:"align-self-center img-thumbnail",src:d,alt:s})),u.a.createElement("div",{className:"media-body align-self-center ml-2 mt-2"},u.a.createElement("h5",null,u.a.createElement(N.a,{target:r,href:a(s)},s)," (".concat(1===i?m:i+" episodes",")")),u.a.createElement("p",null,l)))}T.propTypes={anchorImageFunc:f.a.func,anchorImageTarget:f.a.oneOf(Object.values(N.a.Targets)),anchorTitleFunc:f.a.func,anchorTitleTarget:f.a.oneOf(Object.values(N.a.Targets)),kitsuResult:f.a.object},T.defaultProps={anchorImageFunc:function(e){return Object(j.f)(e)},anchorImageTarget:N.a.Targets.SAME_TAB,anchorTitleFunc:function(e){return Object(j.f)(e)},anchorTitleTarget:N.a.Targets.SAME_TAB};var x=T;function S(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function k(e){var t=e.kitsuResults,n=O()(e,["kitsuResults"]);return t?u.a.createElement("div",{className:"row my-5"},u.a.createElement("div",{className:"col-12 mx-auto"},u.a.createElement("ul",{className:"list-unstyled"},t.data.map((function(e){return u.a.createElement("li",{className:"media row w-75 mb-5 mx-auto",key:e.id},u.a.createElement(x,y()({},n,{kitsuResult:e})))}))))):""}k.propTypes=function(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?S(Object(n),!0).forEach((function(t){g()(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):S(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}({},x.propTypes,{kitsuResults:f.a.object});var P=k,C=n(148);var F=function(){var e="Anime Atsume",t="Search aggregator for many anime shows.",n=Object(i.useState)(""),a=l()(n,2),c=a[0],s=a[1],d=Object(i.useState)(null),f=l()(d,2),p=f[0],h=f[1],b=Object(i.useState)(!1),g=l()(b,2),E=g[0],y=g[1],w=function(){var e=o()(r.a.mark((function e(){var t,n;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return y(!0),t=c.toLowerCase(),e.next=4,Object(m.a)(t);case 4:n=e.sent,h(n),y(!1);case 7:case"end":return e.stop()}}),e)})));return function(){return e.apply(this,arguments)}}(),O=function(e){return"#/show/".concat(encodeURIComponent(e))},N=u.a.createElement("div",{className:"row"},u.a.createElement("div",{className:"col-12 text-center mx-auto mt-5"},u.a.createElement("h1",null,e))),j=u.a.createElement("div",{className:"row mt-3"},u.a.createElement("div",{className:"col-12 col-md-6 text-center mx-auto"},u.a.createElement("h6",null,t))),T=E?u.a.createElement(C.a,{type:C.a.Type.CIRCLE,show:E}):null;return u.a.createElement("div",{className:"text-center mx-auto"},N,j,u.a.createElement(v,{btnDisplay:T,value:c,handleTyping:s,handleSubmit:w}),u.a.createElement(P,{anchorImageFunc:O,anchorTitleFunc:O,kitsuResults:p}))};t.default=F}}]);