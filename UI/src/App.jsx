import { useState } from 'react'

function HelpIcon({ text }) {
  return (
    <div className="group relative inline-flex items-center ml-1">
      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 text-slate-400 hover:text-blue-500 cursor-help" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
      <div className="hidden group-hover:block absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-48 p-2 bg-slate-800 text-white text-xs rounded shadow-lg z-10 text-center">
        {text}
        <div className="absolute top-full left-1/2 -translate-x-1/2 border-opacity-100 border-4 border-transparent border-t-slate-800"></div>
      </div>
    </div>
  )
}

function ValidationForm({ onAdd, onCancel }) {
  const [annotation, setAnnotation] = useState('NotBlank')
  const [message, setMessage] = useState('')
  const [attrs, setAttrs] = useState({})

  const handleAdd = () => {
    let finalAttrs = { ...attrs }
    // Clean up empty attrs based on annotation type requirements
    if (annotation === 'Size') {
      finalAttrs = {
        min: attrs.min ? parseInt(attrs.min) : 0,
        max: attrs.max ? parseInt(attrs.max) : 255
      }
    } else if (annotation === 'Pattern') {
      finalAttrs = { regexp: attrs.regexp || '' }
    } else if (annotation === 'Min' || annotation === 'Max') {
      finalAttrs = { value: attrs.value ? parseInt(attrs.value) : 0 }
    } else {
      finalAttrs = {}
    }

    onAdd({
      annotation,
      message,
      attributes: finalAttrs
    })
  }

  return (
    <div className="bg-slate-50 p-3 rounded border border-slate-200 text-sm mt-3">
      <div className="grid grid-cols-2 gap-3 mb-3">
        <div>
          <label className="block text-xs font-medium text-slate-500 mb-1">Validation Rule</label>
          <select 
            className="w-full border rounded px-2 py-1 outline-indigo-500" 
            value={annotation} 
            onChange={e => { setAnnotation(e.target.value); setAttrs({}); }}
          >
            <option value="NotBlank">@NotBlank</option>
            <option value="NotNull">@NotNull</option>
            <option value="Email">@Email</option>
            <option value="Size">@Size (String Length)</option>
            <option value="Pattern">@Pattern (Regex)</option>
            <option value="Min">@Min (Number)</option>
            <option value="Max">@Max (Number)</option>
          </select>
        </div>
        <div>
          <label className="block text-xs font-medium text-slate-500 mb-1">Error Message</label>
          <input 
            type="text" 
            className="w-full border rounded px-2 py-1 outline-indigo-500" 
            value={message} 
            onChange={e => setMessage(e.target.value)} 
            placeholder="e.g., Must not be empty" 
          />
        </div>
      </div>

      {annotation === 'Size' && (
        <div className="grid grid-cols-2 gap-3 mb-3">
          <div><label className="block text-xs font-medium text-slate-500 mb-1">Min Length</label><input type="number" className="w-full border rounded px-2 py-1" value={attrs.min || ''} onChange={e => setAttrs({...attrs, min: e.target.value})} /></div>
          <div><label className="block text-xs font-medium text-slate-500 mb-1">Max Length</label><input type="number" className="w-full border rounded px-2 py-1" value={attrs.max || ''} onChange={e => setAttrs({...attrs, max: e.target.value})} /></div>
        </div>
      )}
      {annotation === 'Pattern' && (
        <div className="mb-3">
          <label className="block text-xs font-medium text-slate-500 mb-1">Regex Pattern (e.g. ^\d&#123;10&#125;$)</label>
          <input type="text" className="w-full border rounded px-2 py-1" value={attrs.regexp || ''} onChange={e => setAttrs({...attrs, regexp: e.target.value})} />
        </div>
      )}
      {(annotation === 'Min' || annotation === 'Max') && (
        <div className="mb-3">
          <label className="block text-xs font-medium text-slate-500 mb-1">Value Limit</label>
          <input type="number" className="w-full border rounded px-2 py-1" value={attrs.value || ''} onChange={e => setAttrs({...attrs, value: e.target.value})} />
        </div>
      )}

      <div className="flex justify-end gap-2">
        <button onClick={onCancel} className="px-3 py-1 text-xs text-slate-500 hover:text-slate-800">Cancel</button>
        <button onClick={handleAdd} className="px-3 py-1 text-xs bg-indigo-600 text-white rounded hover:bg-indigo-700">Add Rule</button>
      </div>
    </div>
  )
}

function FieldBox({ field, index, updateField }) {
  const [expanded, setExpanded] = useState(false)
  const [showAddVal, setShowAddVal] = useState(false)

  const toggleVal = (key) => {
    updateField(index, key, !field[key])
  }

  const removeValidation = (vIndex) => {
    const updatedVals = [...(field.validations || [])]
    updatedVals.splice(vIndex, 1)
    updateField(index, 'validations', updatedVals)
  }

  const addValidation = (validationObj) => {
    const updatedVals = [...(field.validations || []), validationObj]
    updateField(index, 'validations', updatedVals)
    setShowAddVal(false)
  }

  return (
    <div className="border border-slate-200 rounded-md mb-3 bg-white overflow-hidden shadow-sm">
      <div 
        className="flex justify-between items-center px-4 py-3 bg-slate-50 hover:bg-slate-100 cursor-pointer"
        onClick={() => setExpanded(!expanded)}
      >
        <div className="flex items-center gap-3">
          <svg className={`w-4 h-4 text-slate-400 transition-transform ${expanded ? 'rotate-90' : ''}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
          </svg>
          <span className="font-semibold text-slate-800 text-sm font-mono">{field.name}</span>
          <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-slate-200 text-slate-600">{field.type}</span>
          {field.nullable === false && <span className="text-xs text-red-500 font-bold">*</span>}
        </div>
        <div className="text-xs text-slate-500">
          {(field.validations && field.validations.length > 0) ? `${field.validations.length} validations` : ''}
        </div>
      </div>

      {expanded && (
        <div className="p-4 border-t border-slate-200 border-l-4 border-l-indigo-500">
          <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
            <label className="flex items-center text-xs cursor-pointer"><input type="checkbox" className="mr-2 accent-indigo-600" checked={!!field.searchable} onChange={() => toggleVal('searchable')} /> Searchable (LIKE)</label>
            <label className="flex items-center text-xs cursor-pointer"><input type="checkbox" className="mr-2 accent-indigo-600" checked={!!field.filterable} onChange={() => toggleVal('filterable')} /> Filterable (Exact)</label>
            <label className="flex items-center text-xs cursor-pointer"><input type="checkbox" className="mr-2 accent-indigo-600" checked={!!field.sortable} onChange={() => toggleVal('sortable')} /> Sortable</label>
            <label className="flex items-center text-xs cursor-pointer"><input type="checkbox" className="mr-2 accent-indigo-600" checked={!!field.includeInDropdown} onChange={() => toggleVal('includeInDropdown')} /> In Dropdown API</label>
            <label className="flex items-center text-xs cursor-pointer text-amber-600"><input type="checkbox" className="mr-2 accent-amber-600" checked={!!field.excludeFromRequest} onChange={() => toggleVal('excludeFromRequest')} /> Exclude from Request</label>
            <label className="flex items-center text-xs cursor-pointer text-amber-600"><input type="checkbox" className="mr-2 accent-amber-600" checked={!!field.excludeFromResponse} onChange={() => toggleVal('excludeFromResponse')} /> Exclude from Response</label>
          </div>

          <div>
            <div className="flex justify-between items-center mb-2">
              <h4 className="text-xs font-semibold text-slate-600 uppercase">Validations</h4>
              {!showAddVal && (
                <button 
                  onClick={() => setShowAddVal(true)}
                  className="text-xs text-indigo-600 hover:text-indigo-800 font-medium flex items-center"
                >
                  + Add Rule
                </button>
              )}
            </div>
            
            {(field.validations || []).length === 0 && !showAddVal && (
              <p className="text-xs text-slate-400 italic">No explicit validations (DB constraints apply).</p>
            )}

            <div className="space-y-2">
              {(field.validations || []).map((v, i) => (
                <div key={i} className="flex justify-between items-center bg-slate-50 border border-slate-200 px-3 py-2 rounded text-xs">
                  <div>
                    <span className="font-bold text-slate-700">@{v.annotation}</span>
                    <span className="text-slate-500 ml-2">
                      {v.attributes && Object.entries(v.attributes).map(([k,val]) => `${k}=${val}`).join(', ')}
                    </span>
                    <span className="text-slate-400 ml-3 italic">"{v.message}"</span>
                  </div>
                  <button onClick={() => removeValidation(i)} className="text-red-500 hover:text-red-700">
                     <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              ))}
            </div>

            {showAddVal && <ValidationForm onAdd={addValidation} onCancel={() => setShowAddVal(false)} />}
          </div>
        </div>
      )}
    </div>
  )
}


function App() {
  const [sql, setSql] = useState('')
  const [configStr, setConfigStr] = useState('')
  const [configObj, setConfigObj] = useState(null)
  
  // Tab state for the right pane: 'ui' or 'json'
  const [activeTab, setActiveTab] = useState('ui')
  
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const handleParse = async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await fetch('http://localhost:8080/api/v2/generator/parse-sql', {
        method: 'POST',
        headers: { 'Content-Type': 'text/plain' },
        body: sql
      })

      if (!response.ok) {
        throw new Error('Failed to parse SQL. Please check your syntax.')
      }

      const data = await response.json()
      setConfigObj(data)
      setConfigStr(JSON.stringify(data, null, 2))
      setActiveTab('ui') 
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  // Update a single property dynamically
  const updateConfig = (key, value) => {
    if (!configObj) return
    const updated = { ...configObj, [key]: value }
    setConfigObj(updated)
    setConfigStr(JSON.stringify(updated, null, 2))
  }

  // Deep update a specific field
  const updateField = (index, key, value) => {
    if (!configObj || !configObj.fields) return;
    const newFields = [...configObj.fields];
    newFields[index] = { ...newFields[index], [key]: value };
    updateConfig('fields', newFields);
  }

  const handleGenerate = async () => {
    if (!configStr) return

    setLoading(true)
    setError(null)
    try {
      let parsedConfig;
      try {
        parsedConfig = JSON.parse(configStr)
      } catch(e) {
        throw new Error('Invalid JSON configuration. Please fix the JSON payload before generating.')
      }

      const response = await fetch('http://localhost:8080/api/v2/generator/generate/zip', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(parsedConfig)
      })

      if (!response.ok) {
        throw new Error('Failed to generate code zip.')
      }

      const blob = await response.blob()
      const url = window.URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `${parsedConfig.moduleName || 'module'}-generated-code.zip`
      document.body.appendChild(a)
      a.click()
      a.remove()
      window.URL.revokeObjectURL(url)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-900 font-sans p-4 md:p-8">
      <div className="max-w-[1400px] mx-auto">
        <header className="mb-6 border-b border-slate-200 pb-4">
          <h1 className="text-3xl font-bold tracking-tight text-slate-800">
            Dynamic API Generator
          </h1>
          <p className="text-slate-500 mt-1">Convert SQL schemas directly into production-ready Spring Boot microservices.</p>
        </header>

        {error && (
          <div className="bg-red-50 text-red-600 p-4 rounded-xl mb-6 shadow-sm border border-red-100 flex items-center">
             <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
            </svg>
            <span className="font-semibold">{error}</span>
          </div>
        )}

        <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
          {/* Left Column: SQL Input */}
          <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden flex flex-col h-[750px]">
            <div className="bg-slate-50 px-4 py-3 border-b border-slate-200 flex justify-between items-center">
              <h2 className="font-semibold text-slate-800 text-sm flex items-center gap-2 uppercase tracking-wide">
                <span className="flex items-center justify-center bg-blue-100 text-blue-700 w-5 h-5 rounded-full text-xs font-bold">1</span>
                Input Schema (SQL / DDL)
              </h2>
              <button 
                onClick={handleParse}
                disabled={loading || !sql.trim()}
                className="bg-blue-600 hover:bg-blue-700 disabled:bg-blue-300 text-white px-4 py-1.5 rounded-md text-sm font-medium transition-colors"
              >
                {loading ? 'Processing...' : 'Parse SQL'}
              </button>
            </div>
            <div className="bg-amber-50 px-4 py-2 border-b border-amber-100 text-amber-800 text-xs flex items-center">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-2 text-amber-600 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <span><strong>Note:</strong> Ensure this table is already created in your projected database so the generated code can successfully start and map the JPA repositories.</span>
            </div>
            <textarea
              className="flex-1 w-full p-4 focus:outline-none resize-none font-mono text-sm leading-relaxed text-slate-700 bg-slate-50/30"
              placeholder="CREATE TABLE public.patients (&#10;  id UUID PRIMARY KEY,&#10;  first_name VARCHAR(50) NOT NULL,&#10;  ...&#10;);"
              value={sql}
              onChange={(e) => setSql(e.target.value)}
              spellCheck="false"
            />
          </div>

          {/* Right Column: Configuration UI & Generator */}
          <div className="bg-white rounded-xl shadow-sm border border-slate-200 overflow-hidden flex flex-col h-[750px]">
            <div className="bg-slate-50 px-4 py-3 border-b border-slate-200 flex justify-between items-center z-10">
              <div className="flex items-center gap-4">
                <h2 className="font-semibold text-slate-800 text-sm flex items-center gap-2 uppercase tracking-wide">
                  <span className="flex items-center justify-center bg-indigo-100 text-indigo-700 w-5 h-5 rounded-full text-xs font-bold">2</span>
                  Module Config
                </h2>
                
                {configObj && (
                  <div className="flex bg-slate-200/50 p-1 rounded-md ml-4 text-xs font-medium">
                    <button 
                      onClick={() => setActiveTab('ui')}
                      className={`px-3 py-1 rounded-sm transition-colors ${activeTab === 'ui' ? 'bg-white shadow text-indigo-600' : 'text-slate-500 hover:text-slate-700'}`}
                    >
                      Settings UI
                    </button>
                    <button 
                      onClick={() => setActiveTab('json')}
                      className={`px-3 py-1 rounded-sm transition-colors ${activeTab === 'json' ? 'bg-white shadow text-indigo-600' : 'text-slate-500 hover:text-slate-700'}`}
                    >
                      Raw JSON
                    </button>
                  </div>
                )}
              </div>

              <button 
                onClick={handleGenerate}
                disabled={loading || !configObj}
                className="bg-indigo-600 hover:bg-indigo-700 disabled:bg-indigo-300 text-white px-4 py-1.5 rounded-md text-sm font-medium transition-colors"
              >
                {loading ? 'Generating...' : 'Download Project ZIP'}
              </button>
            </div>
            
            <div className="flex-1 overflow-auto bg-[#f8fafc]">
              {!configObj ? (
                <div className="h-full flex flex-col items-center justify-center text-slate-400 space-y-4">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-10 w-10 text-slate-300" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4" />
                  </svg>
                  <p className="text-sm">Parse a schema first to view settings.</p>
                </div>
              ) : activeTab === 'json' ? (
                <textarea
                  className="w-full h-full p-4 bg-transparent font-mono text-xs text-slate-500 focus:outline-none resize-none leading-relaxed cursor-not-allowed"
                  value={configStr}
                  readOnly
                  spellCheck="false"
                />
              ) : (
                <div className="p-6 space-y-8 pb-12">
                  
                  {/* General Application Section */}
                  <div className="bg-white p-5 border border-slate-200 rounded-lg shadow-sm">
                    <h3 className="font-semibold text-slate-700 mb-4 flex items-center border-b pb-2">
                      Core Identification Settings
                    </h3>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-xs font-medium text-slate-500 mb-1">Module Name</label>
                        <input type="text" className="w-full border rounded-md px-3 py-1.5 text-sm outline-indigo-500" value={configObj.moduleName || ''} onChange={e => updateConfig('moduleName', e.target.value)} />
                      </div>
                      <div>
                        <label className="block text-xs font-medium text-slate-500 mb-1">Root Package Name</label>
                        <input type="text" className="w-full border rounded-md px-3 py-1.5 text-sm outline-indigo-500" value={configObj.packageName || ''} onChange={e => updateConfig('packageName', e.target.value)} />
                      </div>
                      <div>
                        <label className="block text-xs font-medium text-slate-500 mb-1">Database Table</label>
                        <input type="text" className="w-full border rounded-md px-3 py-1.5 text-sm outline-indigo-500" value={configObj.tableName || ''} onChange={e => updateConfig('tableName', e.target.value)} />
                      </div>
                      <div>
                        <label className="block text-xs font-medium text-slate-500 mb-1">Schema Name <HelpIcon text="The database schema (e.g., 'public')" /></label>
                        <input type="text" className="w-full border rounded-md px-3 py-1.5 text-sm outline-indigo-500" value={configObj.schemaName || ''} onChange={e => updateConfig('schemaName', e.target.value)} />
                      </div>
                    </div>
                  </div>

                  {/* Schema Architecture Section */}
                  <div className="bg-white p-5 border border-slate-200 rounded-lg shadow-sm">
                    <h3 className="font-semibold text-slate-700 mb-4 flex items-center border-b pb-2">
                       Schema Architecture
                    </h3>
                    <div className="grid grid-cols-2 lg:grid-cols-3 gap-4 mb-4">
                      <div>
                        <label className="block text-xs font-medium text-slate-500 mb-1">Primary Key</label>
                        <input type="text" className="w-full border rounded-md px-3 py-1.5 text-sm outline-indigo-500" value={configObj.primaryKey || ''} onChange={e => updateConfig('primaryKey', e.target.value)} />
                      </div>
                      <div>
                        <label className="block text-xs font-medium text-slate-500 mb-1">Primary Key Type</label>
                        <select className="w-full border rounded-md px-3 py-1.5 text-sm outline-indigo-500 bg-white" value={configObj.primaryKeyType || 'Long'} onChange={e => updateConfig('primaryKeyType', e.target.value)}>
                          <option value="Long">Long</option>
                          <option value="Integer">Integer</option>
                          <option value="UUID">UUID</option>
                          <option value="String">String</option>
                        </select>
                      </div>
                      <div>
                        <label className="block text-xs font-medium text-slate-500 mb-1">Sequence / Generator</label>
                        <input type="text" className="w-full border rounded-md px-3 py-1.5 text-sm outline-indigo-500" value={configObj.primaryKeySequence || ''} onChange={e => updateConfig('primaryKeySequence', e.target.value)} placeholder="Auto-detect" />
                      </div>
                      <div>
                        <label className="block text-xs font-medium text-slate-500 mb-1">UUID Identifer Field</label>
                        <input type="text" className="w-full border rounded-md px-3 py-1.5 text-sm outline-indigo-500" value={configObj.uuidField || ''} onChange={e => updateConfig('uuidField', e.target.value)} />
                      </div>
                      <div>
                        <label className="block text-xs font-medium text-slate-500 mb-1">Soft Delete Column</label>
                        <input type="text" className="w-full border rounded-md px-3 py-1.5 text-sm outline-indigo-500" value={configObj.softDeleteField || ''} onChange={e => updateConfig('softDeleteField', e.target.value)} />
                      </div>
                    </div>
                    
                    <label className="flex items-center text-sm cursor-pointer hover:bg-slate-50 p-2 rounded-md border min-w-max w-fit px-4 border-slate-200">
                      <input type="checkbox" className="mr-3 w-4 h-4 accent-indigo-600 rounded" checked={!!configObj.auditFields} onChange={e => updateConfig('auditFields', e.target.checked)} />
                      <span>
                        Enable system audit fields
                        <span className="text-slate-400 text-xs ml-2">(createdAt, updatedAt, createdBy)</span>
                      </span>
                    </label>
                  </div>

                  {/* Fields & Validations Engine */}
                  {configObj.fields && configObj.fields.length > 0 && (
                     <div className="bg-white p-5 border border-slate-200 rounded-lg shadow-sm">
                       <h3 className="font-semibold text-slate-700 mb-4 flex items-center border-b pb-2">
                           Domain Fields & Validations
                           <HelpIcon text="Configure API payload inclusion and map deep internal Data Annotations to schema outputs" />
                       </h3>
                       <div className="space-y-3">
                         {configObj.fields.map((field, index) => (
                           <FieldBox key={index} field={field} index={index} updateField={updateField} />
                         ))}
                       </div>
                     </div>
                  )}

                  {/* API Generation Section */}
                  <div className="bg-white p-5 border border-slate-200 rounded-lg shadow-sm">
                    <h3 className="font-semibold text-slate-700 mb-4 flex items-center border-b pb-2">
                       API & Endpoint Capabilities
                    </h3>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                       <label className="flex items-center text-sm cursor-pointer hover:bg-slate-50 p-2 rounded-md border border-slate-200">
                        <input type="checkbox" className="mr-3 w-4 h-4 accent-indigo-600 rounded" checked={!!configObj.enableStatusFilter} onChange={e => updateConfig('enableStatusFilter', e.target.checked)} />
                        <span>Filter by Status <HelpIcon text="Exposes ?status=active/inactive in the GET list API" /></span>
                      </label>
                      <label className="flex items-center text-sm cursor-pointer hover:bg-slate-50 p-2 rounded-md border border-slate-200">
                        <input type="checkbox" className="mr-3 w-4 h-4 accent-indigo-600 rounded" checked={!!configObj.enableCount} onChange={e => updateConfig('enableCount', e.target.checked)} />
                        <span>Enable Metrics Counting <HelpIcon text="Builds GET /count for dashboards and dashboards metrics" /></span>
                      </label>
                       <label className="flex items-center text-sm cursor-pointer hover:bg-slate-50 p-2 rounded-md border border-slate-200">
                        <input type="checkbox" className="mr-3 w-4 h-4 accent-indigo-600 rounded" checked={!!configObj.enableDropdown} onChange={e => updateConfig('enableDropdown', e.target.checked)} />
                        <span>Generic Dropdown List API <HelpIcon text="Generates a minimal DTO projection API for select boxes (Label/Value)" /></span>
                      </label>
                      <label className="flex items-center text-sm cursor-pointer hover:bg-slate-50 p-2 rounded-md border border-slate-200">
                        <input type="checkbox" className="mr-3 w-4 h-4 accent-indigo-600 rounded" checked={!!configObj.enableBulkDelete} onChange={e => updateConfig('enableBulkDelete', e.target.checked)} />
                        <span>Bulk Hard/Soft Deletion <HelpIcon text="Builds POST /bulk/delete" /></span>
                      </label>
                       <label className="flex items-center text-sm cursor-pointer hover:bg-slate-50 p-2 rounded-md border border-slate-200">
                        <input type="checkbox" className="mr-3 w-4 h-4 accent-indigo-600 rounded" checked={!!configObj.enableExport} onChange={e => updateConfig('enableExport', e.target.checked)} />
                        <span>Enable CSV/Excel Export <HelpIcon text="Generates POI data export pipelines" /></span>
                      </label>
                    </div>

                    <div className="mt-4 pt-4 border-t border-slate-100">
                      <label className="block text-xs font-medium text-slate-500 mb-1">
                        Dropdown Reference Label
                        <HelpIcon text="Which column displays physically in the dropdown? (e.g. 'categoryName')" />
                      </label>
                       <input type="text" className="w-full md:w-1/2 border rounded-md px-3 py-1.5 text-sm outline-indigo-500" value={configObj.dropdownLabelField || ''} onChange={e => updateConfig('dropdownLabelField', e.target.value)} disabled={!configObj.enableDropdown} placeholder={configObj.enableDropdown ? "e.g. name" : "Disabled"}/>
                    </div>
                  </div>

                  {/* Advanced / Performance Section */}
                  <div className="bg-white p-5 border border-slate-200 rounded-lg shadow-sm">
                    <h3 className="font-semibold text-slate-700 mb-4 flex items-center border-b pb-2">
                       Advanced / Performance
                    </h3>
                    
                    <div className="grid grid-cols-2 lg:grid-cols-3 gap-4 mb-4">
                      <div>
                         <label className="block text-xs font-medium text-slate-500 mb-1">Default Sort Field</label>
                        <input type="text" className="w-full border rounded-md px-3 py-1.5 text-sm outline-indigo-500" value={configObj.defaultSortField || ''} onChange={e => updateConfig('defaultSortField', e.target.value)} />
                      </div>
                      <div>
                         <label className="block text-xs font-medium text-slate-500 mb-1">Default Sort Mode</label>
                         <select className="w-full border rounded-md px-3 py-1.5 text-sm outline-indigo-500 bg-white" value={configObj.defaultSortDirection || 'DESC'} onChange={e => updateConfig('defaultSortDirection', e.target.value)}>
                          <option value="DESC">DESC (Newest first)</option>
                          <option value="ASC">ASC (Oldest first)</option>
                        </select>
                      </div>
                       <div>
                         <label className="block text-xs font-medium text-slate-500 mb-1">Results Per Page</label>
                        <input type="number" className="w-full border rounded-md px-3 py-1.5 text-sm outline-indigo-500" value={configObj.defaultPageSize || 10} onChange={e => updateConfig('defaultPageSize', parseInt(e.target.value))} />
                      </div>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4 pt-4 border-t border-slate-100">
                      <div>
                         <label className="flex items-center text-sm cursor-pointer mb-2 font-medium text-slate-700">
                          <input type="checkbox" className="mr-3 w-4 h-4 accent-indigo-600 rounded" checked={!!configObj.enableCache} onChange={e => updateConfig('enableCache', e.target.checked)} />
                          Enable Redis/In-Memory Caching (getByUuid)
                        </label>
                        {configObj.enableCache && (
                           <div>
                            <label className="block text-xs font-medium text-slate-500 mb-1 ml-7">Cache Name Identifier</label>
                            <input type="text" className="w-full border rounded-md px-3 py-1.5 text-sm outline-indigo-500 ml-7 w-[calc(100%-1.75rem)]" value={configObj.cacheName || ''} onChange={e => updateConfig('cacheName', e.target.value)} placeholder="Default: same as Module Name" />
                           </div>
                        )}
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4 mt-6 pt-4 border-t border-slate-100">
                       <div>
                         <label className="block text-xs font-medium text-slate-500 mb-1">Exception Code (Not Found)</label>
                        <input type="text" className="w-full border rounded-md px-3 py-1.5 text-sm outline-emerald-600" value={configObj.notFoundErrorCode || ''} onChange={e => updateConfig('notFoundErrorCode', e.target.value)} placeholder="ErrorCode.RESOURCE_NOT_FOUND" />
                      </div>
                      <div>
                         <label className="block text-xs font-medium text-slate-500 mb-1">Exception Code (Duplicates)</label>
                        <input type="text" className="w-full border rounded-md px-3 py-1.5 text-sm outline-emerald-600" value={configObj.duplicateErrorCode || ''} onChange={e => updateConfig('duplicateErrorCode', e.target.value)} placeholder="ErrorCode.DUPLICATE_RESOURCE" />
                      </div>
                    </div>

                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default App
