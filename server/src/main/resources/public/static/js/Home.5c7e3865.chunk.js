(window.webpackJsonp=window.webpackJsonp||[]).push([[1],{599:function(e,t,n){"use strict";n.d(t,"i",(function(){return o})),n.d(t,"e",(function(){return s})),n.d(t,"f",(function(){return l})),n.d(t,"b",(function(){return i})),n.d(t,"a",(function(){return u})),n.d(t,"c",(function(){return m})),n.d(t,"h",(function(){return d})),n.d(t,"d",(function(){return f})),n.d(t,"g",(function(){return p}));var a=n(146),r=n.n(a);function c(e,t){return e+encodeURIComponent(t)}var o="https://kitsu.io/api/edge/anime?filter[text]=",s=function(e){return c(o,e)},l=function(e){return c("https://myanimelist.net/anime.php?q=",e)},i="/searchKissanime",u="/getVideosForEpisode",m=function(e){return c("/corsProxy?url=",e)},d=function(e,t,n,a){return"".concat("/video","/").concat(e,"/").concat(t,"/").concat(n,"?url=").concat(a)},f=function(e){return"".concat("/image","/").concat(e)};function p(e){var t=new URL(e).pathname.split("/"),n=r()(t,4);return{showName:n[2],episodeName:n[3]}}},600:function(e,t,n){"use strict";n.d(t,"d",(function(){return m})),n.d(t,"e",(function(){return d})),n.d(t,"b",(function(){return f})),n.d(t,"c",(function(){return h})),n.d(t,"a",(function(){return v}));n(108);var a=n(595),r=n.n(a),c=n(146),o=n.n(c),s=n(596),l=n.n(s),i=n(2);n(593),n(594);function u(e,t){var n=e.attribute,a=e.value,r=!1,c=!0,o=!1,s=void 0;try{for(var l,i=t[Symbol.iterator]();!(c=(l=i.next()).done);c=!0){var u=l.value;if(u instanceof HTMLElement){var m=u.getAttribute(n);if(m&&m.includes(a)){r=!0;break}}}}catch(e){o=!0,s=e}finally{try{c||null==i.return||i.return()}finally{if(o)throw s}}return r}function m(e){var t=arguments.length>1&&void 0!==arguments[1]?arguments[1]:{},n=t.initialValue,a=void 0===n?null:n,r=t.type,c=void 0===r?"local":r,s=window["".concat(c,"Storage")],u=l()((function(){})),m=Object(i.useState)((function(){var t=s.getItem(e);return t?JSON.parse(t):a})),d=o()(m,2),f=d[0],p=d[1],h=function(t){var n=t;try{l()(t)===u&&(n=t(f)),p(n),s.setItem(e,JSON.stringify(n))}catch(e){console.error("Could not store value (".concat(t,") to ").concat(c,"Storage. Error ="),e)}};return[f,h]}function d(e){var t=arguments.length>1&&void 0!==arguments[1]?arguments[1]:{},n=t.nestedEventField,a=void 0===n?null:n,c=t.initialEventState,s=void 0===c?null:c,u=t.handleEvent,m=void 0===u?null:u,d=t.useEffectInputs,f=void 0===d?[]:d,p=Object(i.useState)(s),h=o()(p,2),v=h[0],b=h[1],g=l()(m)===l()((function(){}));function E(e){var t=a?e[a]:e;g?m(v,b,t):b(t)}return Object(i.useEffect)((function(){return window.addEventListener(e,E),function(){window.removeEventListener(e,E)}}),[e].concat(r()(f))),[v,b]}function f(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:"down";return d("key".concat(e),{nestedEventField:"key"})}function p(){var e=d("click"),t=o()(e,2),n=t[0],a=t[1];return[function(e){if(!e||Array.isArray(e)&&0===e.length)return[];if(e.path)return e.path;for(var t=[],n=e.target;n;)t.push(n),n=n.parentElement;return t.push(document,window),t}(n),a]}function h(e,t){var n=!(arguments.length>2&&void 0!==arguments[2])||arguments[2],a=f(),r=o()(a,2),c=r[0],s=r[1],l=p(),i=o()(l,2),m=i[0],d=i[1],h=n&&"Escape"===c,v=u(e,m),b=u(t,m),g=h||b&&!v,E=function(){s(null),d([])};return[g,E]}function v(e){Object(i.useEffect)((function(){e()?b(!1):b()}),[e])}function b(){var e=!(arguments.length>0&&void 0!==arguments[0])||arguments[0];document.body.style.overflow=e?"auto":"hidden"}},601:function(e,t,n){"use strict";var a=n(85),r=n.n(a),c=n(595),o=n.n(c),s=n(596),l=n.n(s),i=n(2),u=n.n(i),m=n(37),d=n.n(m);function f(e){var t=[e.className],n=[];return e.underlineText&&t.push("underline"),l()(e.rel)===l()("")?n.push(e.rel):l()(e.rel)===l()([])&&n.push.apply(n,o()(e.rel)),u.a.createElement("a",r()({className:t.join(" "),href:e.href,target:e.target,rel:n.join(" ")},e.aria),e.children)}f.Targets={NEW_TAB:"_blank",SAME_TAB:"_self",PARENT:"_parent",TOP:"_top"},f.propTypes={className:d.a.string,href:d.a.string,children:d.a.node,underlineText:d.a.bool,rel:d.a.oneOfType([d.a.string,d.a.arrayOf(d.a.string)]),target:d.a.string,aria:d.a.object},f.defaultProps={className:"",href:"",children:"",underlineText:!0,rel:["noopener","noreferrer"],target:f.Targets.NEW_TAB,aria:{}};var p=f;t.a=p},602:function(e,t,n){"use strict";n.d(t,"a",(function(){return l}));var a=n(593),r=n.n(a),c=n(594),o=n.n(c),s=n(599);function l(e){return i.apply(this,arguments)}function i(){return(i=o()(r.a.mark((function e(t){var n,a;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return e.prev=0,e.next=3,fetch(Object(s.e)(t));case 3:return n=e.sent,e.next=6,n.json();case 6:return e.abrupt("return",e.sent);case 9:return e.prev=9,e.t0=e.catch(0),console.log("Error in fetching Kitsu results: "+e.t0),console.log("Attempting to get them through CORS proxy."),e.next=15,fetch(Object(s.c)(s.i+t));case 15:return a=e.sent,e.next=18,a.json();case 18:return e.abrupt("return",e.sent);case 19:case"end":return e.stop()}}),e,null,[[0,9]])})))).apply(this,arguments)}},603:function(e,t,n){var a={"./apple-touch-icon.png":[604,4],"./favicon-144.png":[605,5],"./favicon-192.png":[606,6],"./favicon.ico":[607,7],"./favicon.png":[608,8],"./fonts/BrushScript.eot":[609,9],"./fonts/BrushScript.ttf":[610,10],"./fonts/BrushScript.woff":[611,11],"./react_logo.svg":[612,12]};function r(e){if(!n.o(a,e))return Promise.resolve().then((function(){var t=new Error("Cannot find module '"+e+"'");throw t.code="MODULE_NOT_FOUND",t}));var t=a[e],r=t[0];return n.e(t[1]).then((function(){return n.t(r,7)}))}r.keys=function(){return Object.keys(a)},r.id=603,e.exports=r},613:function(e,t,n){"use strict";n.r(t);var a=n(593),r=n.n(a),c=n(85),o=n.n(c),s=n(108),l=n.n(s),i=n(594),u=n.n(i),m=n(146),d=n.n(m),f=n(2),p=n.n(f),h=n(37),v=n.n(h),b=n(602),g=n(599);function E(e){return y.apply(this,arguments)}function y(){return(y=u()(r.a.mark((function e(t){return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return e.next=2,fetch(g.b,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({title:t})}).then((function(e){return e.json()}));case 2:return e.abrupt("return",e.sent);case 3:case"end":return e.stop()}}),e)})))).apply(this,arguments)}var w=n(600),O=n(147),N=n(595),j=n.n(N),T=n(596),x=n.n(T);function S(e){var t=e.className,n=e.title,a=e.children,r=e.footer,c=e.escapeClosesModal,o=e.useGridForBody,s=e.useGridForFooter,l=e.preventDocumentScrolling,i=e.show,u=e.showCloseButton,m=e.onClose,h=e.forwardRef,v=Object(f.useState)(!1),b=d()(v,2),g=b[0],E=b[1],y=Object(w.c)({attribute:"class",value:"modal-content"},{attribute:"class",value:"modal fade"},c),O=d()(y,2),N=O[0],j=O[1],T=function(){E(!0),setTimeout((function(){m(),E(!1)}),500)};N&&(j(),i&&T()),Object(w.a)((function(){return i&&l}));var S=i&&!g?"show":"",k=i?"":"0%",P=x()(n)===x()("")?p.a.createElement("h4",{className:"margin-clear"},n):n;return p.a.createElement("div",{className:"modal fade d-block ".concat(S),style:{background:"rgba(0, 0, 0, 0.7)",width:k,height:k}},p.a.createElement("div",{className:"modal-dialog modal-dialog-centered width-fit",style:{maxWidth:"90vw"}},p.a.createElement("div",{className:"modal-content overflow-auto "+t,style:{maxHeight:"90vh"},ref:h},p.a.createElement("div",{className:"modal-header"},p.a.createElement("div",{className:"modal-title"},P),u&&p.a.createElement("button",{className:"close",onClick:T},p.a.createElement("span",null,"×"))),p.a.createElement("div",{className:"modal-body"},p.a.createElement("div",{className:o?"container-fluid":""},a)),r&&p.a.createElement("div",{className:"modal-footer"},p.a.createElement("div",{className:s?"container-fluid":""},r)))))}S.propTypes={className:v.a.string,title:v.a.node,children:v.a.node,footer:v.a.node,escapeClosesModal:v.a.bool,useGridForBody:v.a.bool,useGridForFooter:v.a.bool,preventDocumentScrolling:v.a.bool,show:v.a.bool,showCloseButton:v.a.bool,onClose:v.a.func,forwardRef:v.a.object},S.defaultProps={className:"",title:"",children:"",footer:"",escapeClosesModal:!0,useGridForBody:!0,useGridForFooter:!0,preventDocumentScrolling:!0,show:!1,showCloseButton:!0,onClose:function(){}};var k=S;function P(e){var t=Object(f.useState)(5),n=d()(t,2),a=n[0],r=n[1],c=Object(w.e)("keydown"),s=d()(c,2),l=s[0],i=s[1],u=Object(f.useRef)(null);return l&&function(){var e=u.current;if(e){switch(l.key){case"ArrowLeft":e.currentTime-=a;break;case"ArrowRight":e.currentTime+=a;break;case"ArrowUp":l.shiftKey?r(a+1):e.volume<=.95?e.volume+=.05:e.volume=1;break;case"ArrowDown":l.shiftKey?r(a-1):e.volume>=.05?e.volume-=.05:e.volume=0;break;case"f":e.requestFullscreen();break;case" ":e.paused?e.play():e.pause()}i(null)}}(),Object(f.useEffect)((function(){u.current&&(u.current.onfocus=function(){return u.current.blur()})}),[u]),p.a.createElement("video",o()({className:e.className,controls:!0,autoPlay:!0,ref:u},e.videoElementProps),p.a.createElement("source",{src:e.src,type:e.type}))}P.propTypes={className:v.a.string,src:v.a.string,type:v.a.string,videoElementProps:v.a.object},P.defaultProps={className:"",src:"",type:"video/mp4",videoElementProps:{}};var C=P;function F(e){return e.show?p.a.createElement("div",{className:"".concat(e.className," ").concat(e.fullScreen?"full-screen-minus-scrollbar":"")},p.a.createElement("div",{className:e.fullScreen?"absolute-center":""},p.a.createElement("h3",{className:"mr-1"},"Sorry, something went wrong."),p.a.createElement("h3",null,e.suggestion))):""}F.propTypes={className:v.a.string,fullScreen:v.a.bool,show:v.a.bool,suggestion:v.a.node},F.defaultProps={className:"",fullScreen:!1,show:!1,suggestion:"Try refreshing the page."};var A=F,D=n(601);function I(e){return U.apply(this,arguments)}function U(){return(U=u()(r.a.mark((function e(t){var n,a=arguments;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return n=a.length>1&&void 0!==a[1]?a[1]:null,e.next=3,fetch(g.a,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({episodeUrl:t,captchaAnswers:n})}).then((function(e){return e.json()}));case 3:return e.abrupt("return",e.sent);case 4:case"end":return e.stop()}}),e)})))).apply(this,arguments)}function R(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function L(e){var t={hasError:!1,showSpinner:!1,captchaPrompts:[],captchaOptions:[],captchaAnswers:[],videoOptions:[],captchaImagesLoaded:new Set,videoHostUrl:null},n=Object(f.useState)(t.hasError),a=d()(n,2),c=a[0],o=a[1],s=Object(f.useState)(t.showSpinner),i=d()(s,2),m=i[0],h=i[1],v=Object(f.useState)(t.captchaPrompts),b=d()(v,2),E=b[0],y=b[1],w=Object(f.useState)(t.captchaOptions),N=d()(w,2),T=N[0],x=N[1],S=Object(f.useState)(t.captchaAnswers),P=d()(S,2),F=P[0],U=P[1],L=Object(f.useState)(t.videoOptions),B=d()(L,2),_=B[0],H=B[1],M=Object(f.useState)(t.captchaImagesLoaded),G=d()(M,2),J=G[0],K=G[1],W=Object(f.useState)(t.videoHostUrl),V=d()(W,2),q=V[0],z=V[1],Y=Object(f.useRef)(null),Q=_.length>0,X=function(){o(t.hasError),y(t.captchaPrompts),x(t.captchaOptions),U(t.captchaAnswers),H(t.videoOptions),K(t.captchaImagesLoaded),z(t.videoHostUrl)};function Z(e,t){return $.apply(this,arguments)}function $(){return($=u()(r.a.mark((function e(t,n){var a,c,s,l;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return h(!0),X(),e.prev=2,e.next=5,I(t,n);case 5:if(!((a=e.sent).status&&a.status>299)){e.next=8;break}throw"Got HTTP status code ".concat(a.status," from server. Error: ").concat(a.error,".");case 8:c=a.data,s=a.captchaContent,l=a.videoHostUrl,s&&(y(s.promptTexts),x(s.imgIdsAndSrcs)),c&&H(c),l&&z(l),h(!1),e.next=19;break;case 15:e.prev=15,e.t0=e.catch(2),console.error("Error fetching for episodes:",e.t0),o(!0);case 19:case"end":return e.stop()}}),e,null,[[2,15]])})))).apply(this,arguments)}Object(f.useEffect)((function(){e.show&&e.episodeUrl&&Z(e.episodeUrl)}),[e.show,e.episodeUrl]);var ee=function(){var t=F.map((function(e,t){var n=T.find((function(t){return t.formId===e})),a=E[t],r=function(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?R(Object(n),!0).forEach((function(t){l()(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):R(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}({},n);return r.promptText=a,r}));Z(e.episodeUrl,t)},te=function(e){return F.includes(e)},ne=function(e){te(e)?U([]):function(e){U((function(t){var n=j()(t);return n.push(e),n}))}(e),Y.current.scrollTo(0,0)};Object(f.useEffect)((function(){e.show&&e.episodeUrl&&F.length>0&&F.length===E.length&&ee()}),[e.show,e.episodeUrl,F.length,E.length]);var ae=F.length,re=E.length?p.a.createElement("div",{className:"text-center"},p.a.createElement("h4",null,"Please solve this captcha"),p.a.createElement("h5",{className:"d-inline"},"(",ae+1,"/",E.length,")"),p.a.createElement("h5",{className:"text-danger d-inline ml-1"},E[ae])):e.episodeTitle,ce="";return e.show&&e.episodeUrl&&(c?ce=p.a.createElement(A,{suggestion:"Try closing and re-opening the modal.",show:c}):m?ce=p.a.createElement(O.a,{className:"w-40 h-40",show:!0}):E.length?ce=p.a.createElement(p.a.Fragment,null,T.map((function(e,t){var n=e.formId,a=e.imageId;return p.a.createElement(p.a.Fragment,{key:n},p.a.createElement("img",{className:te(n)?"border border-medium border-primary rounded":"",src:Object(g.d)(a),onClick:function(){return ne(n)},onLoad:function(){return e=t,void K((function(t){var n=new Set(t);return n.add(e),n}));var e},alt:"failed to load. sorry"}),t%2==0?"":p.a.createElement(p.a.Fragment,null,p.a.createElement("br",null),p.a.createElement("br",null)),p.a.createElement(O.a,{show:!J.has(t)}))}))):Q?ce=function(){if(Q){var t=Object(g.g)(e.episodeUrl),n=t.showName,a=t.episodeName,r=_[0],c=r.label,o=r.file;return p.a.createElement(C,{className:"w-100",src:Object(g.h)(n,a,c,o),videoElementProps:e.videoElementProps})}}():null!=q&&(ce=p.a.createElement(A,{suggestion:p.a.createElement(p.a.Fragment,null,p.a.createElement("div",null,"The scraper for this host has not been made yet."),p.a.createElement("div",null,"You may watch the video at:"),p.a.createElement(D.a,{className:"d-block",href:q},q)),show:null!=q}))),p.a.createElement(k,{className:"scroll-smooth",escapeClosesModal:!Q,show:e.show,title:re,onClose:function(){X(),e.onClose()},forwardRef:Y},p.a.createElement("div",{className:"overflow-auto ".concat(m?"d-flex justify-content-center align-items-center":""),style:{minHeight:"200px"}},ce))}L.propTypes={episodeTitle:v.a.string,episodeUrl:v.a.string,show:v.a.bool,onClose:v.a.func,videoElementProps:v.a.object},L.defaultProps={episodeTitle:"",episodeUrl:null,show:!1,onClose:function(){},videoElementProps:{}};var B=L;function _(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function H(e){var t=decodeURIComponent(e.title),n=Object(f.useState)(!1),a=d()(n,2),c=a[0],s=a[1],i=Object(f.useState)(null),m=d()(i,2),h=m[0],v=m[1],y=Object(f.useState)(null),N=d()(y,2),j=N[0],T=N[1],x=Object(f.useState)(0),S=d()(x,2),k=S[0],P=S[1],C=Object(f.useState)(null),F=d()(C,2),I=F[0],U=F[1],R=Object(f.useState)(null),L=d()(R,2),H=L[0],M=L[1],G=Object(w.d)("showsProgress",{initialValue:{}}),J=d()(G,2),K=J[0],W=J[1];function V(){return(V=u()(r.a.mark((function e(){var n,a,c;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return e.next=2,Object(b.a)(t.toLowerCase());case 2:n=e.sent,a=n.data,c=a.find((function(e){return e.attributes.canonicalTitle===t})),v(c);case 6:case"end":return e.stop()}}),e)})))).apply(this,arguments)}function q(){return(q=u()(r.a.mark((function e(){var n;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return e.prev=0,e.next=3,E(t);case 3:if(!((n=e.sent).status&&n.status>299)){e.next=6;break}throw"Got HTTP status code ".concat(n.status," from server. Error: ").concat(n.error,".");case 6:null!=n.results&&n.results.length>=0&&n.results.forEach((function(e){e.episodes.reverse()})),T(n),e.next=14;break;case 10:e.prev=10,e.t0=e.catch(0),console.error("Error fetching for show matches:",e.t0),s(!0);case 14:case"end":return e.stop()}}),e,null,[[0,10]])})))).apply(this,arguments)}Object(f.useEffect)((function(){!function(){V.apply(this,arguments)}(),function(){q.apply(this,arguments)}()}),[]);var z=function(){return j.results[I].title};var Y=function(e,t){var n=e.title,a=e.url,r=K[z()]===n;return p.a.createElement("a",{className:"list-group-item cursor-pointer ".concat(r?"active":"text-primary"),key:t,onClick:function(){return M({episodeTitle:n,episodeUrl:a})}},n)},Q=function(e,t){var n=e.title,a=e.episodes,r=p.a.createElement("h4",null,p.a.createElement("span",{className:"ml-1 d-xs-none badge badge-pill badge-".concat(I===t?"dark":"primary")},a.length));return p.a.createElement("button",{className:"list-group-item remove-focus-highlight ".concat(I===t?"active":""),key:t,onClick:function(){return U(t)}},p.a.createElement("div",{className:"d-sm-none d-xs-flex justify-content-between align-items-center"},p.a.createElement("h5",{className:"mb-2"},n),r),p.a.createElement("div",{className:"d-xs-none d-sm-flex justify-content-between align-items-center"},p.a.createElement("h3",{className:"mb-2"},n),r))};if(!h||!j)return p.a.createElement(O.a,{fullScreen:!0,show:!0});var X=h.attributes,Z=X.canonicalTitle,$=X.synopsis,ee=X.episodeCount,te=X.showType,ne=X.posterImage.small,ae=[{tabTitle:"Overview",content:p.a.createElement("div",{className:"row"},p.a.createElement("div",{className:"col-centered col-lg-4 my-3 d-flex"},p.a.createElement("img",{className:"my-auto flex-center",src:ne,alt:Z,style:{maxWidth:"95%"}})),p.a.createElement("div",{className:"col-sm-12 col-lg-8 d-flex justify-content-center"},p.a.createElement("div",{className:"text-center"},p.a.createElement("div",{className:"row mb-3"},p.a.createElement("div",{className:"col"},p.a.createElement("h5",{className:"capitalize-first"},1===ee?te:ee+" episodes"))),p.a.createElement("div",{className:"row mb-3"},p.a.createElement("div",{className:"col"},p.a.createElement("p",null,$))),p.a.createElement("div",{className:"row mb-3"},p.a.createElement("div",{className:"col"},p.a.createElement(D.a,{href:Object(g.f)(t)},p.a.createElement("h5",null,"View on MyAnimeList")))))))},{tabTitle:"Watch",content:null==j.results||0===j.results.length?p.a.createElement("div",{className:"row d-flex justify-content-center"},p.a.createElement("h4",null,"Sorry, no episodes were found for this show.")):p.a.createElement("div",{className:"row"},p.a.createElement("div",{className:"col-6"},p.a.createElement("div",{className:"d-xs-block d-sm-none"},p.a.createElement("h4",{className:"mb-2"},"Shows")),p.a.createElement("div",{className:"d-xs-none d-sm-block"},p.a.createElement("h3",{className:"mb-2 d-inline-block"},"Shows"),p.a.createElement("h4",{className:"d-inline-block ml-1"},"(# episodes)")),p.a.createElement("div",{className:"text-left list-group overflow-auto",style:{maxHeight:"400px"}},j.results.map(Q))),p.a.createElement("div",{className:"col-6"},p.a.createElement("div",{className:"d-xs-block d-sm-none"},p.a.createElement("h4",{className:"mb-2"},"Episodes")),p.a.createElement("div",{className:"d-xs-none d-sm-block"},p.a.createElement("h3",{className:"mb-2"},"Episodes")),function(){if(!j||null==I)return null;var e=z(),t=K[e];return t?p.a.createElement("div",{className:"row mb-3"},p.a.createElement("div",{className:"col-12"},p.a.createElement("h5",{className:"mb-1"},"Last watched: "),p.a.createElement("h5",{className:"underline"},t))):void 0}(),p.a.createElement("div",{className:"text-left list-group overflow-auto",style:{maxHeight:"400px"}},null!=I&&j.results[I].episodes.map(Y))))}],re=p.a.createElement("nav",null,p.a.createElement("ul",{className:"pagination"},ae.map((function(e,t){var n=e.tabTitle;return p.a.createElement("li",{className:"page-item ".concat(k===t?"active":""),key:t,onClick:function(){return P(t)},style:{width:"".concat(100/ae.length,"%")}},p.a.createElement("a",{className:"page-link cursor-pointer"},n))}))));return c?p.a.createElement(A,{fullScreen:!0,show:c}):p.a.createElement(p.a.Fragment,null,p.a.createElement("div",{className:"row pb-3"},p.a.createElement("h1",{className:"text-center mx-auto mt-5"},t)),p.a.createElement("div",{className:"row pt-5"},p.a.createElement("div",{className:"col-12"},p.a.createElement("div",{className:"card mb-5"},re,p.a.createElement("div",{className:"card-body"},ae[k].content)))),p.a.createElement(B,o()({},H,{show:null!=H,onClose:function(){return M(null)},videoElementProps:{onLoadStart:function(){var e=z(),t=H.episodeTitle;W((function(n){var a=function(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?_(Object(n),!0).forEach((function(t){l()(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):_(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}({},n);return a[e]=t,a}))}}})))}H.propTypes={title:v.a.string},H.defaultProps={title:""};var M=H;t.default=M},614:function(e,t,n){"use strict";n.r(t);var a=n(593),r=n.n(a),c=n(594),o=n.n(c),s=n(146),l=n.n(s),i=n(2),u=n.n(i),m=n(602),d=n(37),f=n.n(d),p=n(600);function h(e){var t=Object(p.b)(),n=l()(t,2),a=n[0],r=n[1];"Enter"===a&&(e.handleSubmit(),r(null));var c=u.a.createElement("span",null,u.a.createElement("i",{className:"fas fa-search"})),o=e.btnDisplay?e.btnDisplay:c;return u.a.createElement("div",{className:"row mt-3 mb-5"},u.a.createElement("div",{className:"col-12 col-md-6 mx-auto"},u.a.createElement("div",{className:"input-group my-3"},u.a.createElement("input",{className:"form-control input-large remove-focus-highlight",type:"text",placeholder:'e.g. "Kimi no na wa"',value:e.value,onChange:function(t){var n=t.target.value;e.handleTyping(n)}}),u.a.createElement("div",{className:"input-group-append"},u.a.createElement("button",{className:"btn btn-outline-secondary remove-focus-highlight",onClick:function(){return e.handleSubmit()}},o)))))}h.propTypes={btnDisplay:f.a.node,value:f.a.string,handleTyping:f.a.func,handleSubmit:f.a.func},h.defaultProps={btnDisplay:null,value:"",handleTyping:function(){},handleSubmit:function(){}};var v=h,b=n(108),g=n.n(b),E=n(85),y=n.n(E),w=n(597),O=n.n(w),N=n(601),j=n(599);function T(e){var t=e.anchorImageFunc,n=e.anchorImageTarget,a=e.anchorTitleFunc,r=e.anchorTitleTarget,c=e.kitsuResult;if(!c||!c.attributes)return"";var o=c.attributes,s=o.canonicalTitle,l=o.synopsis,i=o.episodeCount,m=o.showType,d=o.posterImage.small;return u.a.createElement(u.a.Fragment,null,u.a.createElement(N.a,{target:n,href:t(s)},u.a.createElement("img",{className:"align-self-center img-thumbnail",src:d,alt:s})),u.a.createElement("div",{className:"media-body align-self-center ml-2 mt-2"},u.a.createElement("h5",null,u.a.createElement(N.a,{target:r,href:a(s)},s)," (".concat(1===i?m:i+" episodes",")")),u.a.createElement("p",null,l)))}T.propTypes={anchorImageFunc:f.a.func,anchorImageTarget:f.a.oneOf(Object.values(N.a.Targets)),anchorTitleFunc:f.a.func,anchorTitleTarget:f.a.oneOf(Object.values(N.a.Targets)),kitsuResult:f.a.object},T.defaultProps={anchorImageFunc:function(e){return Object(j.f)(e)},anchorImageTarget:N.a.Targets.SAME_TAB,anchorTitleFunc:function(e){return Object(j.f)(e)},anchorTitleTarget:N.a.Targets.SAME_TAB};var x=T;function S(e,t){var n=Object.keys(e);if(Object.getOwnPropertySymbols){var a=Object.getOwnPropertySymbols(e);t&&(a=a.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),n.push.apply(n,a)}return n}function k(e){var t=e.kitsuResults,n=O()(e,["kitsuResults"]);return t?u.a.createElement("div",{className:"row my-5"},u.a.createElement("div",{className:"col-12 mx-auto"},u.a.createElement("ul",{className:"list-unstyled"},t.data.map((function(e){return u.a.createElement("li",{className:"media row w-75 mb-5 mx-auto",key:e.id},u.a.createElement(x,y()({},n,{kitsuResult:e})))}))))):""}k.propTypes=function(e){for(var t=1;t<arguments.length;t++){var n=null!=arguments[t]?arguments[t]:{};t%2?S(Object(n),!0).forEach((function(t){g()(e,t,n[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(n)):S(Object(n)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(n,t))}))}return e}({},x.propTypes,{kitsuResults:f.a.object});var P=k,C=n(147);var F=function(){var e="Anime Atsume",t="Search aggregator for many anime shows.",n=Object(i.useState)(""),a=l()(n,2),c=a[0],s=a[1],d=Object(i.useState)(null),f=l()(d,2),p=f[0],h=f[1],b=Object(i.useState)(!1),g=l()(b,2),E=g[0],y=g[1],w=function(){var e=o()(r.a.mark((function e(){var t,n;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return y(!0),t=c.toLowerCase(),e.next=4,Object(m.a)(t);case 4:n=e.sent,h(n),y(!1);case 7:case"end":return e.stop()}}),e)})));return function(){return e.apply(this,arguments)}}(),O=function(e){return"#/show/".concat(encodeURIComponent(e))},N=u.a.createElement("div",{className:"row"},u.a.createElement("div",{className:"col-12 text-center mx-auto mt-5"},u.a.createElement("h1",null,e))),j=u.a.createElement("div",{className:"row mt-3"},u.a.createElement("div",{className:"col-12 col-md-6 text-center mx-auto"},u.a.createElement("h6",null,t))),T=E?u.a.createElement(C.a,{type:C.a.Type.CIRCLE,show:E}):null;return u.a.createElement("div",{className:"text-center mx-auto"},N,j,u.a.createElement(v,{btnDisplay:T,value:c,handleTyping:s,handleSubmit:w}),u.a.createElement(P,{anchorImageFunc:O,anchorTitleFunc:O,kitsuResults:p}))};t.default=F}}]);