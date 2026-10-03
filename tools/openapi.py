"""Generate the checked-in OpenAPI contract without third-party Python packages."""
import json
from pathlib import Path
S={}
def ref(name): return {'$ref':'#/components/schemas/'+name}
def string(**kw): return {'type':'string',**kw}
def integer(**kw): return {'type':'integer','format':'int64',**kw}
def obj(name, props, required=None):
 S[name]={'type':'object','properties':props,'required':list(props) if required is None else required}
uuid=string(format='uuid'); timestamp=string(format='date-time'); qty={'type':'number','minimum':0,'multipleOf':0.01,'maximum':9999999999999999.99}
obj('Login',{'username':string(minLength=1,maxLength=100),'password':string(minLength=1,maxLength=200,writeOnly=True)})
obj('RefreshBody',{'refreshToken':string(minLength=1,maxLength=4096,writeOnly=True)})
obj('Tokens',{'accessToken':string(),'refreshToken':string(),'expiresIn':integer()})
obj('RegisterUser',{'username':string(pattern=r'^[A-Za-z0-9][A-Za-z0-9._-]{2,99}$'),'password':string(minLength=12,maxLength=72,writeOnly=True,description='At most 72 UTF-8 bytes'),'fullName':string(minLength=1,maxLength=255),'email':string(format='email',maxLength=255,nullable=True)},['username','password','fullName'])
obj('RegisteredUser',{'id':integer(),'username':string(),'storeId':integer(),'role':string(enum=['EMPLOYEE'])})
obj('Register',{'deviceCode':string(minLength=1,maxLength=100),'deviceName':string(minLength=1,maxLength=255),'fcmToken':string(maxLength=4096,nullable=True)},['deviceCode','deviceName'])
obj('Registration',{'deviceId':integer(),'deviceSecret':string()})
obj('Token',{'fcmToken':string(minLength=1,maxLength=4096)})
obj('Device',{'id':integer(),'deviceCode':string(),'deviceName':string(),'reachable':{'type':'boolean'},'lastActiveAt':string()})
obj('FcmHealth',{'fallbackRequired':{'type':'boolean'},'reason':string(enum=['FCM_DISABLED','NO_TOKEN','PUSH_FAILED','READY'])})
obj('FinderConfiguration',{'fcmEnabled':{'type':'boolean'},'schedulerEnabled':{'type':'boolean'}})
obj('DeviceFinderCommand',{'requestId':uuid,'command':string(enum=['FIND','STOP']),'expiresAt':timestamp})
obj('Find',{'deviceId':integer(minimum=1)})
obj('Stop',{'requestId':uuid})
obj('AlertEvent',{'requestId':uuid,'status':string(enum=['RINGING','STOPPED','FAILED'])})
obj('FindRequest',{'id':uuid,'requesterId':integer(nullable=True),'storeId':integer(),'deviceId':integer(),'status':string(enum=['QUEUED','SENT','RINGING','STOPPED','FAILED','EXPIRED']),'expiresAt':timestamp})
obj('WebDeviceRow',{'storeId':integer(),'storeCode':string(),'storeName':string(),'deviceId':integer(nullable=True),'deviceCode':string(nullable=True),'deviceName':string(nullable=True),'lastActiveAt':string(format='date-time',nullable=True),'hasPushToken':{'type':'boolean'},'requestId':string(format='uuid',nullable=True),'status':string(nullable=True),'expiresAt':string(format='date-time',nullable=True)})
S['WebDeviceRow']['properties'].update({'lastEvent':string(nullable=True),'lastEventMessage':string(nullable=True)})
obj('Product',{'barcode':string(),'productCode':string(),'productName':string(),'imageUrl':string(nullable=True)})
obj('ImageSync',{'productCode':string(minLength=1,maxLength=50),'imageUrl':string(nullable=True,maxLength=1000,pattern=r'^https://[^\s]+$'),'sourceVersion':integer(minimum=1)},['productCode','sourceVersion'])
obj('Inventory',{'id':integer(),'productId':integer(),'storeId':integer(),'quantity':qty,'version':integer(minimum=0)})
obj('Adjustment',{'requestId':uuid,'productCode':string(minLength=1,maxLength=50),'quantity':qty,'version':integer(minimum=0),'reason':string(minLength=1,maxLength=1000)})
obj('DisposalLine',{'productCode':string(minLength=1,maxLength=50),'quantity':{**qty,'exclusiveMinimum':True},'reason':string(minLength=1,maxLength=500)})
obj('DisposalCreate',{'requestId':uuid,'remarks':string(minLength=1,maxLength=1000),'items':{'type':'array','minItems':1,'maxItems':100,'items':ref('DisposalLine')}})
obj('Transition',{'version':integer(minimum=0)})
obj('Disposal',{'id':uuid,'storeId':integer(),'status':string(enum=['PENDING','CONFIRMED','CANCELLED']),'remarks':string(),'createdBy':integer(),'version':integer(),'createdAt':timestamp})
obj('DisposalItem',{'productId':integer(),'quantity':qty,'reason':string()})
obj('DisposalDetail',{'disposal':ref('Disposal'),'items':{'type':'array','items':ref('DisposalItem')},'history':{'type':'array','items':{'type':'object'}}})
obj('Problem',{'type':string(),'title':string(),'status':integer(),'detail':string(),'instance':string()},['type','title','status'])
paths={}
def route(method,path,name,request=None,response=None,role='Authenticated',array=False,params=None):
 security=[] if role=='Public' else [{'DeviceId':[],'DeviceSecret':[]}] if role=='Device' else [{'Bearer':[]}]
 spec={'operationId':name,'summary':name,'description':'Required permission: '+role,'security':security,'responses':{'200':{'description':'Success'}}}
 if request:spec['requestBody']={'required':True,'content':{'application/json':{'schema':ref(request)}}}
 if response:
  schema=ref(response) if response!='Row' else {'type':'object'}
  if array:schema={'type':'array','items':schema}
  spec['responses']['200']['content']={'application/json':{'schema':schema}}
 for status,desc in [('400','Validation'),('401','Authentication'),('403','Permission'),('404','Not found in current store'),('409','Version, state, or duplicate conflict'),('429','Ingress rate limit')]:
  spec['responses'][status]={'description':desc,'content':{'application/problem+json':{'schema':ref('Problem')}}}
 import re
 spec['parameters']=[{'name':p,'in':'path','required':True,'schema':uuid if p=='id' else string()} for p in re.findall(r'\{(.*?)\}',path)]
 if params:spec['parameters']+=params
 paths.setdefault(path,{})[method]=spec
route('post','/auth/login','login','Login','Tokens','Public')
route('post','/auth/refresh','refresh','RefreshBody','Tokens','Public')
route('post','/auth/register','registerEmployee','RegisterUser','RegisteredUser','MANAGER')
paths['/auth/register']['post']['responses']['201'] = paths['/auth/register']['post']['responses'].pop('200')
paths['/auth/register']['post']['responses']['201']['description'] = 'Employee created in the authenticated manager store'
route('get','/devices','listDevices',response='Device',role='MANAGER',array=True)
route('post','/devices/register','registerDevice','Register','Registration','MANAGER or EMPLOYEE')
route('get','/web/finder/devices','webFinderDevices',response='WebDeviceRow',role='Public',array=True)
route('get','/web/finder/configuration','webFinderConfiguration',response='FinderConfiguration',role='Public')
route('delete','/web/finder/devices/{deviceId}','webDeleteDevice',role='Public')
paths['/web/finder/devices/{deviceId}']['delete']['parameters'][0]['schema'] = integer()
paths['/web/finder/devices/{deviceId}']['delete']['responses']['204'] = {'description':'Device and associated finder history/outbox deleted; general audit retained'}
del paths['/web/finder/devices/{deviceId}']['delete']['responses']['200']
route('post','/web/finder/devices/{deviceId}/find','webFindDevice',response='FindRequest',role='Public')
paths['/web/finder/devices/{deviceId}/find']['post']['parameters'][0]['schema'] = integer()
route('get','/web/finder/requests/{id}','webFindStatus',response='FindRequest',role='Public')
route('post','/web/finder/requests/{id}/stop','webStopFinder',role='Public')
route('put','/devices/token','refreshDeviceToken','Token',role='Device')
route('post','/pda/find','findDevice','Find','FindRequest','MANAGER')
route('get','/pda/find/{id}','findStatus',response='FindRequest',role='MANAGER')
route('post','/pda/stop','stopFinder','Stop',role='MANAGER')
route('get','/pda/fcm-health','finderFcmHealth',response='FcmHealth',role='Device')
route('get','/pda/commands','pollFinderCommands',response='DeviceFinderCommand',role='Device',array=True)
route('post','/pda/events','acknowledgeFinder','AlertEvent',role='Device')
route('get','/products/barcode/{barcode}','lookupProduct',response='Product')
route('put','/products/image-sync','publishImage','ImageSync',role='ERP')
route('get','/inventories/{productCode}','getInventory',response='Inventory')
route('post','/inventory-adjustments','adjustInventory','Adjustment','Inventory','MANAGER')
route('get','/inventory-transactions','inventoryHistory',response='Row',role='MANAGER or ERP',array=True,params=[{'name':'afterId','in':'query','schema':integer(minimum=0,default=0)}])
route('get','/disposals','listDisposals',response='Disposal',array=True,params=[{'name':'page','in':'query','schema':integer(minimum=0,maximum=10000,default=0)}])
route('get','/disposals/{id}','disposalDetail',response='DisposalDetail')
route('post','/disposals','createDisposal','DisposalCreate','Disposal','MANAGER or EMPLOYEE')
for action in ['confirm','cancel']:route('post','/disposals/{id}/'+action,action+'Disposal','Transition','Disposal','MANAGER')
doc={'openapi':'3.0.3','info':{'title':'Retail PDA API','version':'1.0.0'},'servers':[{'url':'https://pda.example.com'}],'paths':paths,'components':{'securitySchemes':{'Bearer':{'type':'http','scheme':'bearer','bearerFormat':'JWT'},'DeviceId':{'type':'apiKey','in':'header','name':'X-Device-Id'},'DeviceSecret':{'type':'apiKey','in':'header','name':'X-Device-Secret'}},'schemas':S}}
(Path(__file__).resolve().parents[1]/'docs/openapi.json').write_text(json.dumps(doc,indent=2)+'\n',encoding='utf-8')
