const assert=require('assert');
const fs=require('fs');
const vm=require('vm');
const code=fs.readFileSync('src/main/webapp/resources/js/menu-category.js','utf8');
function setup(authenticated) {
    const xhrs=[], opened=[];
    const feedback={hidden:true,text:{textContent:''},login:{hidden:true},querySelector(s){return s==='.bookmark-feedback-text'?this.text:this.login;}};
    const attrs={'data-bookmark-authenticated':String(authenticated),'data-csrf-header':'X-CSRF-TOKEN','data-csrf-token':'test-token'};
    const root={getAttribute:n=>attrs[n],querySelector:()=>feedback};
    const buttons=['1','2'].map(id=>{
        const data={'data-menu-id':id,'data-menu-name':'food '+id,'data-bookmarked':'false','aria-pressed':'false','data-add-url':'/controller/menu/'+id+'/bookmark/add','data-remove-url':'/controller/menu/'+id+'/bookmark/remove'};
        const count={textContent:'0'}, classes=new Set(['fa-heart-o']);
        const icon={classList:{toggle(c,on){if(on)classes.add(c);else classes.delete(c);}}};
        return {data,count,classes,disabled:false,getAttribute:n=>data[n],setAttribute(n,v){data[n]=v;},removeAttribute(n){delete data[n];},querySelector:()=>icon,
            closest:s=>s==='.food-category'?root:{querySelector:()=>count},addEventListener(n,fn){this[n]=fn;}};
    });
    function Xhr(){xhrs.push(this);this.headers={};this.open=(method,url)=>{this.method=method;this.url=url;};this.setRequestHeader=(k,v)=>{this.headers[k]=v;};this.send=()=>{this.sent=true;};}
    vm.runInNewContext(code,{document:{querySelectorAll:s=>s==='.bookmark-button'?buttons:[]},window:{addEventListener(){},open:()=>opened.push(true)},XMLHttpRequest:Xhr});
    function click(i=0){let prevented=false,stopped=false;buttons[i].click({preventDefault(){prevented=true;},stopPropagation(){stopped=true;}});assert(prevented&&stopped);}
    function respond(index,status,data,url){const x=xhrs[index];x.status=status;x.responseText=typeof data==='string'?data:JSON.stringify(data);x.responseURL=url||x.url;x.onload();}
    return {buttons,xhrs,opened,feedback,attrs,click,respond};
}
let app=setup(false);app.click();assert.equal(app.xhrs.length,0);assert.equal(app.feedback.login.hidden,false);assert.equal(app.opened.length,0);
app=setup(true);app.click();app.click();assert.equal(app.xhrs.length,1);assert(app.buttons[0].disabled);assert.equal(app.xhrs[0].headers['X-CSRF-TOKEN'],'test-token');assert.equal(app.xhrs[0].method,'POST');
app.click(1);assert.equal(app.xhrs.length,2);app.respond(0,200,{menuId:1,bookmarked:true,bookmarkCnt:7});
assert.equal(app.buttons[0].count.textContent,'7');assert.equal(app.buttons[0].data['aria-pressed'],'true');assert(app.buttons[0].classes.has('fa-heart'));assert(!app.buttons[0].disabled);assert(app.buttons[1].disabled);
app.respond(1,200,{menuId:2,bookmarked:true,bookmarkCnt:2});app.click();assert(app.xhrs[2].url.endsWith('/remove'));app.respond(2,200,{menuId:1,bookmarked:false,bookmarkCnt:6});
assert.equal(app.buttons[0].count.textContent,'6');assert(app.buttons[0].classes.has('fa-heart-o'));assert.equal(app.buttons[1].count.textContent,'2');assert.equal(app.opened.length,0);
for(const failure of [()=>app.respond(app.xhrs.length-1,500,{}),()=>app.respond(app.xhrs.length-1,404,{}),()=>app.respond(app.xhrs.length-1,200,'<html>error</html>'),()=>app.respond(app.xhrs.length-1,200,{menuId:99,bookmarked:true,bookmarkCnt:8}),()=>app.respond(app.xhrs.length-1,200,{menuId:1,bookmarked:true,bookmarkCnt:-1}),()=>app.xhrs[app.xhrs.length-1].onerror(),()=>app.xhrs[app.xhrs.length-1].ontimeout()]) {
    app.click();failure();assert.equal(app.buttons[0].count.textContent,'6');assert.equal(app.buttons[0].data['aria-pressed'],'false');assert(!app.buttons[0].disabled);assert(!app.feedback.hidden);
}
app.click();app.respond(app.xhrs.length-1,200,'login','http://localhost:8080/controller/customLogin');assert.equal(app.feedback.login.hidden,false);assert.equal(app.buttons[0].data['aria-pressed'],'false');
const before=app.xhrs.length;app.attrs['data-csrf-token']='';app.click();assert.equal(app.xhrs.length,before);
console.log('PASS: bookmark login guidance, independent click events/rows, CSRF header, duplicate-click guard, authoritative count, add/remove icons, failure preservation, network timeout and expired-session recovery');
