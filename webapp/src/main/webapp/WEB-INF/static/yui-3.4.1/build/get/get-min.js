/*
YUI 3.4.1 (build 4118)
Copyright 2011 Yahoo! Inc. All rights reserved.
Licensed under the BSD License.
http://yuilibrary.com/license/
*/
YUI.add("get",function(e){var B=e.UA,p=e.Lang,b="text/javascript",v="text/css",I="stylesheet",s="script",q="autopurge",A="utf-8",w="link",C="async",h=true,l={script:h,css:!(B.webkit||B.gecko)},z={},r=0,g,u=function(J){var K=J.timer;if(K){clearTimeout(K);J.timer=null;}},m=function(M,J,P,N){var K=N||e.config.win,O=K.document,Q=O.createElement(M),L;if(P){e.mix(J,P);}for(L in J){if(J[L]&&J.hasOwnProperty(L)){Q.setAttribute(L,J[L]);}}return Q;},k=function(K,L,J){return m(w,{id:e.guid(),type:v,rel:I,href:K},J,L);},E=function(K,L,J){return m(s,{id:e.guid(),type:b,src:K},J,L);},a=function(K,L,J){return{tId:K.tId,win:K.win,data:K.data,nodes:K.nodes,msg:L,statusText:J,purge:function(){d(this.tId);}};},o=function(N,M,J){var L=z[N],K=L&&L.onEnd;L.finished=true;if(K){K.call(L.context,a(L,M,J));}},F=function(M,L){var K=z[M],J=K.onFailure;u(K);if(J){J.call(K.context,a(K,L));}o(M,L,"failure");},y=function(J){F(J,"transaction "+J+" was aborted");},x=function(L){var J=z[L],K=J.onSuccess;u(J);if(J.aborted){y(L);}else{if(K){K.call(J.context,a(J));}o(L,undefined,"OK");}},H=function(J,M){var K=z[M],L=(p.isString(J))?K.win.document.getElementById(J):J;if(!L){F(M,"target node not found: "+J);}return L;},d=function(O){var K,R,S,T,L,Q,P,N,M,J=z[O];if(J){K=J.nodes;M=K.length;for(N=0;N<M;N++){L=K[N];S=L.parentNode;if(L.clearAttributes){L.clearAttributes();}else{for(Q in L){if(L.hasOwnProperty(Q)){delete L[Q];}}}S.removeChild(L);}}J.nodes=[];},t=function(N,J){var K=z[N],L=K.onProgress,M;if(L){M=a(K);M.url=J;L.call(K.context,M);}},D=function(L){var J=z[L],K=J.onTimeout;if(K){K.call(J.context,a(J));}o(L,"timeout","timeout");},f=function(M,J){var L=z[M],K=(L&&!L.async);if(!L){return;}if(K){u(L);}t(M,J);if(!L.finished){if(L.aborted){y(M);}else{if((--L.remaining)===0){x(M);}else{if(K){i(M);}}}}},c=function(K,M,L,J){if(B.ie){M.onreadystatechange=function(){var N=this.readyState;if("loaded"===N||"complete"===N){M.onreadystatechange=null;f(L,J);}};}else{if(B.webkit){if(K===s){M.addEventListener("load",function(){f(L,J);},false);}}else{M.onload=function(){f(L,J);};M.onerror=function(N){F(L,N+": "+J);};}}},G=function(L,P,O){var M=z[P],N=O.document,J=M.insertBefore||N.getElementsByTagName("base")[0],K;if(J){K=H(J,P);if(K){K.parentNode.insertBefore(L,K);}}else{N.getElementsByTagName("head")[0].appendChild(L);}},i=function(Q){var O=z[Q],L=O.type,K=O.attributes,P=O.win,N=O.timeout,M,J;if(O.url.length>0){J=O.url.shift();if(N&&!O.timer){O.timer=setTimeout(function(){D(Q);},N);}if(L===s){M=E(J,P,K);}else{M=k(J,P,K);}O.nodes.push(M);c(L,M,Q,J);G(M,Q,P);if(!l[L]){f(Q,J);}if(O.async){i(Q);}}},n=function(){if(g){return;}g=true;var J,K;for(J in z){if(z.hasOwnProperty(J)){K=z[J];if(K.autopurge&&K.finished){d(K.tId);delete z[J];}}}g=false;},j=function(K,J,L){L=L||{};var O="q"+(r++),N=L.purgethreshold||e.Get.PURGE_THRESH,M;if(r%N===0){n();}M=z[O]=e.merge(L);M.tId=O;M.type=K;M.url=J;M.finished=false;M.nodes=[];M.win=M.win||e.config.win;M.context=M.context||M;M.autopurge=(q in M)?M.autopurge:(K===s)?true:false;M.attributes=M.attributes||{};M.attributes.charset=L.charset||M.attributes.charset||A;if(C in M&&K===s){M.attributes.async=M.async;}M.url=(p.isString(M.url))?[M.url]:M.url;if(!M.url[0]){M.url.shift();}M.remaining=M.url.length;i(O);return{tId:O};};e.Get={PURGE_THRESH:20,abort:function(K){var L=(p.isString(K))?K:K.tId,J=z[L];if(J){J.aborted=true;}},script:function(J,K){return j(s,J,K);},css:function(J,K){return j("css",J,K);}};},"3.4.1",{requires:["yui-base"]});