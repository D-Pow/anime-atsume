(window.webpackJsonp=window.webpackJsonp||[]).push([[4],{617:function(e,t,a){"use strict";a.r(t);var n=a(596),r=a.n(n),o=a(86),s=a.n(o),c=a(109),l=a.n(c),i=a(597),u=a.n(i),m=a(148),d=a.n(m),p=a(2),f=a.n(p),h=a(37),b=a.n(h),v=a(605),E=a(602);function w(e){return g.apply(this,arguments)}function g(){return(g=u()(r.a.mark((function e(t){return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return e.next=2,fetch(E.b,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({title:t})}).then((function(e){return e.json()}));case 2:return e.abrupt("return",e.sent);case 3:case"end":return e.stop()}}),e)})))).apply(this,arguments)}var y=a(603),N=a(606),O=a(149),j=a(150),x=a(599),S=a.n(x),k=a(598),P=a.n(k);function T(e){var t=e.className,a=e.title,n=e.children,r=e.footer,o=e.escapeClosesModal,s=e.useGridForBody,c=e.useGridForFooter,l=e.preventDocumentScrolling,i=e.show,u=e.showCloseButton,m=e.onClose,h=e.forwardRef,b=Object(p.useState)(!1),v=d()(b,2),E=v[0],w=v[1],g=Object(y.d)({attribute:"class",value:"modal-content"},{attribute:"class",value:"modal fade"},o),N=d()(g,2),j=N[0],x=N[1],S=function(){w(!0),setTimeout((function(){m(),w(!1)}),500)};j&&(x(),i&&S()),Object(y.a)((function(){return i&&l}));var k=i&&!E?"show":"",T=i?"":"0%",C=P()(a)===P()("")?f.a.createElement("h4",{className:"margin-clear"},a):a;return f.a.createElement("div",{className:"modal fade d-flex flex-center ".concat(k),style:{background:"rgba(0, 0, 0, 0.7)",width:T,height:T}},f.a.createElement("div",{className:"modal-dialog modal-dialog-centered flex-center width-fit",style:{maxWidth:"90vw"}},f.a.createElement("div",{className:"modal-content overflow-auto "+t,style:{maxHeight:"90vh"},ref:h},f.a.createElement("div",{className:"modal-header",style:Object(O.b)()?{display:"-webkit-box"}:{}},f.a.createElement("div",{className:"modal-title"},C),u&&f.a.createElement("button",{className:"close",onClick:S},f.a.createElement("span",null,"×"))),f.a.createElement("div",{className:"modal-body"},f.a.createElement("div",{className:s?"container-fluid":""},n)),r&&f.a.createElement("div",{className:"modal-footer"},f.a.createElement("div",{className:c?"container-fluid":""},r)))))}T.propTypes={className:b.a.string,title:b.a.node,children:b.a.node,footer:b.a.node,escapeClosesModal:b.a.bool,useGridForBody:b.a.bool,useGridForFooter:b.a.bool,preventDocumentScrolling:b.a.bool,show:b.a.bool,showCloseButton:b.a.bool,onClose:b.a.func,forwardRef:b.a.object},T.defaultProps={className:"",title:"",children:"",footer:"",escapeClosesModal:!0,useGridForBody:!0,useGridForFooter:!0,preventDocumentScrolling:!0,show:!1,showCloseButton:!0,onClose:function(){}};var C=T;function U(e){var t=Object(p.useState)(5),a=d()(t,2),n=a[0],r=a[1],o=Object(y.f)("keydown"),c=d()(o,2),l=c[0],i=c[1],u=Object(p.useRef)(null);return e.videoRef&&(u=e.videoRef),l&&function(){var e=u.current;if(e){switch(l.key){case"ArrowLeft":e.currentTime-=n;break;case"ArrowRight":e.currentTime+=n;break;case"ArrowUp":l.shiftKey?r(n+1):e.volume<=.95?e.volume+=.05:e.volume=1;break;case"ArrowDown":l.shiftKey?r(n-1):e.volume>=.05?e.volume-=.05:e.volume=0;break;case"f":e.requestFullscreen();break;case" ":e.paused?e.play():e.pause()}i(null)}}(),Object(p.useEffect)((function(){u.current&&(u.current.onfocus=function(){return u.current.blur()})}),[u]),f.a.createElement("video",s()({className:e.className,controls:!0,autoPlay:!0,ref:u},e.videoElementProps),f.a.createElement("source",{src:e.src,type:e.type}))}U.propTypes={className:b.a.string,src:b.a.string,type:b.a.string,videoElementProps:b.a.object,videoRef:b.a.object},U.defaultProps={className:"",src:"",type:"video/mp4",videoElementProps:{}};var F=U;function I(e){return e.show?f.a.createElement("div",{className:"".concat(e.className," ").concat(e.fullScreen?"full-screen-minus-scrollbar":"")},f.a.createElement("div",{className:e.fullScreen?"absolute-center":""},f.a.createElement("h3",{className:"mr-1"},"Sorry, something went wrong."),f.a.createElement("h3",null,e.suggestion))):""}I.propTypes={className:b.a.string,fullScreen:b.a.bool,show:b.a.bool,suggestion:b.a.node},I.defaultProps={className:"",fullScreen:!1,show:!1,suggestion:"Try refreshing the page."};var D=I,R=a(604);function H(e){var t=e.displayElement,a="";return e.centered&&(a="absolute-center top-20"),f.a.createElement("div",{className:"text-center w-100 ".concat(a," ").concat(e.className)},f.a.createElement(t,{className:"w-80 m-auto"},e.text))}H.propTypes={className:b.a.string,centered:b.a.bool,displayElement:b.a.string,text:b.a.string},H.defaultProps={className:"",centered:!0,displayElement:"h1",text:"Please use a modern browser (Chrome, Firefox) to view this website."};var A=H;function L(e){return B.apply(this,arguments)}function B(){return(B=u()(r.a.mark((function e(t){var a,n=arguments;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return a=n.length>1&&void 0!==n[1]?n[1]:null,e.next=3,fetch(E.a,{method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify({episodeUrl:t,captchaAnswers:a})}).then((function(e){return e.json()}));case 3:return e.abrupt("return",e.sent);case 4:case"end":return e.stop()}}),e)})))).apply(this,arguments)}function G(e,t){var a=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),a.push.apply(a,n)}return a}function M(e){var t={hasError:!1,showSpinner:!1,captchaPrompts:[],captchaOptions:[],captchaAnswers:[],videoOptions:[],captchaImagesLoaded:new Set,videoHostUrl:null},a=Object(p.useState)(t.hasError),n=d()(a,2),o=n[0],s=n[1],c=Object(p.useState)(t.showSpinner),i=d()(c,2),m=i[0],h=i[1],b=Object(p.useState)(t.captchaPrompts),v=d()(b,2),w=v[0],g=v[1],y=Object(p.useState)(t.captchaOptions),N=d()(y,2),x=N[0],k=N[1],P=Object(p.useState)(t.captchaAnswers),T=d()(P,2),U=T[0],I=T[1],H=Object(p.useState)(t.videoOptions),B=d()(H,2),M=B[0],J=B[1],z=Object(p.useState)(t.captchaImagesLoaded),V=d()(z,2),W=V[0],K=V[1],q=Object(p.useState)(t.videoHostUrl),Y=d()(q,2),Q=Y[0],X=Y[1],Z=Object(p.useRef)(null),$=Object(p.useRef)(null),_=M.length>0,ee=function(){s(t.hasError),g(t.captchaPrompts),k(t.captchaOptions),I(t.captchaAnswers),J(t.videoOptions),K(t.captchaImagesLoaded),X(t.videoHostUrl)};function te(e,t){return ae.apply(this,arguments)}function ae(){return(ae=u()(r.a.mark((function e(t,a){var n,o,c,l;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return h(!0),ee(),e.prev=2,e.next=5,L(t,a);case 5:if(!((n=e.sent).status&&n.status>299)){e.next=8;break}throw"Got HTTP status code ".concat(n.status," from server. Error: ").concat(n.error,".");case 8:o=n.data,c=n.captchaContent,l=n.videoHostUrl,c&&(g(c.promptTexts),k(c.imgIdsAndSrcs)),o&&J(o),l&&X(l),h(!1),e.next=19;break;case 15:e.prev=15,e.t0=e.catch(2),console.error("Error fetching for episodes:",e.t0),s(!0);case 19:case"end":return e.stop()}}),e,null,[[2,15]])})))).apply(this,arguments)}Object(p.useEffect)((function(){e.show&&e.episodeUrl&&te(e.episodeUrl)}),[e.show,e.episodeUrl]);var ne=function(){var t=U.map((function(e,t){var a=x.find((function(t){return t.formId===e})),n=w[t],r=function(e){for(var t=1;t<arguments.length;t++){var a=null!=arguments[t]?arguments[t]:{};t%2?G(Object(a),!0).forEach((function(t){l()(e,t,a[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(a)):G(Object(a)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(a,t))}))}return e}({},a);return r.promptText=n,r}));te(e.episodeUrl,t)},re=function(e){return U.includes(e)},oe=function(e){re(e)?I([]):function(e){I((function(t){var a=S()(t);return a.push(e),a}))}(e),Z.current.scrollTo(0,0)};Object(p.useEffect)((function(){e.show&&e.episodeUrl&&U.length>0&&U.length===w.length&&ne()}),[e.show,e.episodeUrl,U.length,w.length]);var se=U.length,ce=w.length?f.a.createElement("div",{className:"text-center"},f.a.createElement("h4",null,"Please solve this captcha"),f.a.createElement("h5",{className:"d-inline"},"(",se+1,"/",w.length,")"),f.a.createElement("h5",{className:"text-danger d-inline ml-1"},w[se])):e.episodeTitle,le="";return e.show&&e.episodeUrl&&(o?le=f.a.createElement(D,{suggestion:"Try closing and re-opening the modal.",show:o}):m?le=f.a.createElement(j.a,{className:"w-40 h-40",show:!0}):w.length?le=f.a.createElement(f.a.Fragment,null,x.map((function(e,t){var a=e.formId,n=e.imageId;return f.a.createElement(f.a.Fragment,{key:a},f.a.createElement("img",{className:re(a)?"border border-medium border-primary rounded":"",src:Object(E.d)(n),onClick:function(){return oe(a)},onLoad:function(){return e=t,void K((function(t){var a=new Set(t);return a.add(e),a}));var e},alt:"failed to load. sorry"}),t%2==0?"":f.a.createElement(f.a.Fragment,null,f.a.createElement("br",null),f.a.createElement("br",null)),f.a.createElement(j.a,{show:!W.has(t)}))}))):_?le=function(){if(_){var t=Object(E.g)(e.episodeUrl),a=t.showName,n=t.episodeName,r=M[0],o=r.label,s=r.file;return f.a.createElement("div",null,f.a.createElement(F,{className:"w-100",src:Object(E.h)(a,n,o,s),videoElementProps:e.videoElementProps,videoRef:$}),f.a.createElement("div",null,Object(O.b)()&&$.current&&$.current.readyState<3?f.a.createElement(A,{centered:!1,displayElement:"h3",text:"If the video doesn't load, then use a modern browser (Chrome, Firefox) to view this website."}):""))}}():null!=Q&&(le=f.a.createElement(D,{suggestion:f.a.createElement(f.a.Fragment,null,f.a.createElement("div",null,"The scraper for this host has not been made yet."),f.a.createElement("div",null,"You may watch the video at:"),f.a.createElement(R.a,{className:"d-block",href:Q},Q)),show:null!=Q}))),f.a.createElement(C,{className:m?"overflow-hidden":"",escapeClosesModal:!_,show:e.show,title:ce,onClose:function(){ee(),e.onClose()},forwardRef:Z},f.a.createElement("div",{className:m?"d-flex justify-content-center align-items-center":"overflow-auto",style:{minHeight:"200px"}},le))}M.propTypes={episodeTitle:b.a.string,episodeUrl:b.a.string,show:b.a.bool,onClose:b.a.func,videoElementProps:b.a.object},M.defaultProps={episodeTitle:"",episodeUrl:null,show:!1,onClose:function(){},videoElementProps:{}};var J=M;function z(e,t){var a=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);t&&(n=n.filter((function(t){return Object.getOwnPropertyDescriptor(e,t).enumerable}))),a.push.apply(a,n)}return a}function V(e){var t=decodeURIComponent(e.title),a=Object(p.useState)(!1),n=d()(a,2),o=n[0],c=n[1],i=Object(p.useState)(null),m=d()(i,2),h=m[0],b=m[1],g=Object(p.useState)(null),x=d()(g,2),S=x[0],k=x[1],P=Object(p.useState)(0),T=d()(P,2),C=T[0],U=T[1],F=Object(p.useState)(null),I=d()(F,2),H=I[0],A=I[1],L=Object(p.useState)(null),B=d()(L,2),G=B[0],M=B[1],V=Object(y.e)("showsProgress",{initialValue:{}}),W=d()(V,2),K=W[0],q=W[1];function Y(){return(Y=u()(r.a.mark((function e(){var a,n,o;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return e.next=2,Object(v.a)(t.toLowerCase());case 2:a=e.sent,n=a.data,o=n.find((function(e){return e.attributes.canonicalTitle===t})),n&&!o&&(o=n[0]),b(o);case 7:case"end":return e.stop()}}),e)})))).apply(this,arguments)}function Q(){return(Q=u()(r.a.mark((function e(){var a;return r.a.wrap((function(e){for(;;)switch(e.prev=e.next){case 0:return e.prev=0,e.next=3,w(t);case 3:if(!((a=e.sent).status&&a.status>299)){e.next=6;break}throw"Got HTTP status code ".concat(a.status," from server. Error: ").concat(a.error,".");case 6:null!=a.results&&a.results.length>=0&&a.results.forEach((function(e){e.episodes.reverse()})),k(a),e.next=14;break;case 10:e.prev=10,e.t0=e.catch(0),console.error("Error fetching for show matches:",e.t0),c(!0);case 14:case"end":return e.stop()}}),e,null,[[0,10]])})))).apply(this,arguments)}Object(p.useEffect)((function(){!function(){Y.apply(this,arguments)}(),function(){Q.apply(this,arguments)}()}),[]);var X=function(){var e=arguments.length>0&&void 0!==arguments[0]?arguments[0]:H;return S.results[e].title},Z=function(e,t){return"".concat(e,"-").concat(t)};var $=Object(N.a)((function(e){document.getElementById(e).scrollIntoView({block:"center",inline:"center"})}),500),_=Object(N.a)((function(e){var t=document.getElementById(e);t.parentElement.scrollTop=t.offsetTop-t.offsetHeight}),500);if(o)return f.a.createElement(D,{fullScreen:!0,show:o});if(!h||!S)return f.a.createElement(j.a,{fullScreen:!0,show:!0});var ee=h.attributes,te=ee.canonicalTitle,ae=ee.synopsis,ne=ee.episodeCount,re=ee.showType,oe=ee.posterImage.small,se=[{tabTitle:"Overview",content:f.a.createElement("div",{className:"row"},f.a.createElement("div",{className:"col-centered col-lg-4 my-3 d-flex"},f.a.createElement("img",{className:"my-auto flex-center",src:oe,alt:te,style:{maxWidth:"95%"}})),f.a.createElement("div",{className:"col-sm-12 col-lg-8 d-flex justify-content-center"},f.a.createElement("div",{className:"text-center"},f.a.createElement("div",{className:"row mb-3"},f.a.createElement("div",{className:"col"},f.a.createElement("h5",{className:"capitalize-first"},1===ne?re:ne+" episodes"))),f.a.createElement("div",{className:"row mb-3"},f.a.createElement("div",{className:"col"},f.a.createElement("p",null,ae))),f.a.createElement("div",{className:"row mb-3"},f.a.createElement("div",{className:"col"},f.a.createElement(R.a,{href:Object(E.f)(t)},f.a.createElement("h5",null,"View on MyAnimeList")))))))},{tabTitle:"Watch",content:function(){if(null==S.results||0===S.results.length)return f.a.createElement("div",{className:"row d-flex justify-content-center"},f.a.createElement("h4",null,"Sorry, no episodes were found for this show."));var e=S.results.map((function(e,t){var a=e.title,n=e.episodes,r=f.a.createElement("h4",null,f.a.createElement("span",{className:"ml-1 badge badge-pill badge-".concat(H===t?"dark":"primary")},n.length)),o=Object(O.b)()?"div":"button";return f.a.createElement(o,{className:"btn list-group-item remove-focus-highlight ".concat(H===t?"active":""),id:Z(t,a),key:t,onClick:function(){return A(t)}},f.a.createElement("div",{className:"d-flex justify-content-between align-items-center"},f.a.createElement("h5",{className:"mb-2 d-flex d-sm-none"},a),f.a.createElement("h3",{className:"mb-2 d-none d-sm-flex"},a),r))})),t=function(){if(null!=H)return S.results[H].episodes.map((function(e,t){var a=e.title,n=e.url,r=K[X()]===a;return f.a.createElement("a",{className:"list-group-item cursor-pointer ".concat(r?"active":"text-primary"),id:Z(H,a),key:t,onClick:function(){return M({episodeTitle:a,episodeUrl:n})}},a)}))}(),a=e&&e.length?"border-top border-bottom":"",n=t&&t.length?"border-top border-bottom":"";return f.a.createElement("div",{className:"row"},f.a.createElement("div",{className:"col-sm-12 col-md-6 mb-5"},f.a.createElement("div",null,f.a.createElement("h3",{className:"mb-2 d-none d-sm-inline-block"},"Shows"),f.a.createElement("h4",{className:"mb-2 d-inline-block d-sm-none"},"Shows"),f.a.createElement("h4",{className:"d-inline-block ml-1"},"(# episodes)")),f.a.createElement("div",{className:"text-left list-group overflow-auto ".concat(a," fix-strange-z-index-scrollbars scroll-auto"),style:{maxHeight:"400px"}},e)),f.a.createElement("div",{className:"col-sm-12 col-md-6"},f.a.createElement("div",null,f.a.createElement("h3",{className:"mb-2 d-none d-sm-block"},"Episodes"),f.a.createElement("h4",{className:"mb-2 d-block d-sm-none"},"Episodes")),f.a.createElement("div",{className:"text-left list-group overflow-auto ".concat(n," fix-strange-z-index-scrollbars"),style:{maxHeight:"400px"}},t)))}()}],ce=f.a.createElement("nav",null,f.a.createElement("ul",{className:"pagination"},se.map((function(e,t){var a=e.tabTitle;return f.a.createElement("li",{className:"page-item ".concat(C===t?"active":""),key:t,onClick:function(){return U(t)},style:{width:"".concat(100/se.length,"%")}},f.a.createElement("a",{className:"page-link cursor-pointer"},a))}))));return f.a.createElement("div",{className:"col-12"},f.a.createElement("div",{className:"row pb-4"},f.a.createElement("h1",{className:"text-center mx-auto mt-5"},t)),function(){if(!S||1!==C)return null;var e=(S.results?S.results.map((function(e,t){var a=e.title;return{episodeTitle:K[a],showTitle:a,showIndex:t}})).filter((function(e){return null!=e.episodeTitle})):[]).map((function(e){var t=e.showTitle,a=e.showIndex,n=e.episodeTitle;return f.a.createElement("div",{className:"row mb-1",key:a},f.a.createElement("div",{className:"col-12"},f.a.createElement("span",{className:"h5"},f.a.createElement("span",{className:"underline"},t),":"),f.a.createElement("button",{className:"btn btn-link remove-focus-highlight border-0",onClick:function(){return function(e,t){var a=Z(e,X(e)),n=Z(e,t);A(e),_(a),$(n)}(a,n)}},f.a.createElement("h5",{className:"m-0"},n))))}));return e.length?f.a.createElement("div",{className:"row pb-2"},f.a.createElement("div",{className:"col-12"},f.a.createElement("h4",{className:"mb-2"},"Last watched episodes:"),e)):void 0}(),f.a.createElement("div",{className:"row pt-5 px-2"},f.a.createElement("div",{className:"col-12"},f.a.createElement("div",{className:"card mb-5"},ce,f.a.createElement("div",{className:"card-body"},se[C].content)))),f.a.createElement(J,s()({},G,{show:null!=G,onClose:function(){return M(null)},videoElementProps:{onLoadStart:function(){var e=X(),t=G.episodeTitle;q((function(a){var n=function(e){for(var t=1;t<arguments.length;t++){var a=null!=arguments[t]?arguments[t]:{};t%2?z(Object(a),!0).forEach((function(t){l()(e,t,a[t])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(a)):z(Object(a)).forEach((function(t){Object.defineProperty(e,t,Object.getOwnPropertyDescriptor(a,t))}))}return e}({},a);return n[e]=t,n}))}}})))}V.propTypes={title:b.a.string},V.defaultProps={title:""};var W=V;t.default=W}}]);