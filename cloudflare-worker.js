export default {
    // ai declaration: here we used claude and gpt to implement the cloudflare worker AI endpoint
    // with llama 3 model integration and expense analysis prompt engineering
    async fetch(request, env) {
      // Log incoming request
      console.log(`[Request] ${request.method} ${new URL(request.url).pathname}`);
      
      // Handle CORS preflight request
      if (request.method === "OPTIONS") {
        console.log("[CORS] Handling preflight request");
        return handleCors(request);
      }
      
      // Check if this is a POST request with expense data
      if (request.method === "POST") {
        try {
          console.log("[POST] Processing expense data request");
          
          // Parse the expense data from the request
          const expenseData = await request.json();
          
          if (!expenseData.expenses || !Array.isArray(expenseData.expenses) || expenseData.expenses.length === 0) {
            console.error("[Error] Invalid expense data format", expenseData);
            return corsResponse({ 
              success: false, 
              error: "No expenses provided or invalid format" 
            }, { status: 400 });
          }
          
          console.log(`[Expenses] Processing ${expenseData.expenses.length} expenses`);
          
          // Format expenses for the prompt
          const expensesText = expenseData.expenses.map(expense => 
            `- ${expense.category}: ${expense.amount} on ${expense.date} for '${expense.description}'`
          ).join("\n");
          
          // ai declaration: here we used claude and gpt to design the AI prompt for financial analysis
          // with gen Z tone and ZAR currency context for better user engagement
          // Create the prompt for expense analysis
          const prompt = `
            Based on these recent expenses:
            
            ${expensesText}
            
            Provide a brief, helpful financial insight and suggestion for the user. Consider:
            1. Spending patterns or categories with high expenses
            2. Practical saving tips based on their spending habits
            3. Any notable trends or irregularities
            4. All finances are actually in ZAR not USD, it'll say $300 for example but its actually R300
            
            Keep the response concise (max 2 sentences) and actionable. Respond in a gen Z chill vibe tone. Dont start with introductions or anything, just go straight into business.
          `;
          
          console.log("[AI] Sending request to Llama model");
          
          // Call AI model with chat format
          const aiResponse = await env.AI.run('@cf/meta/llama-3-8b-instruct', {
            messages: [
              { role: 'system', content: 'You are a helpful gen Z financial advisor.' },
              { role: 'user', content: prompt }
            ]
          });
          
          console.log("[AI] Received response from Llama model");
          console.log(`[AI Response] ${aiResponse.response.substring(0, 100)}${aiResponse.response.length > 100 ? '...' : ''}`);
          
          // Return the AI-generated insight with CORS headers
          return corsResponse({
            success: true,
            insight: aiResponse.response
          });
        } catch (error) {
          console.error("[Error]", error);
          return corsResponse({ 
            success: false, 
            error: error.message || "Failed to process expense data" 
          }, { status: 500 });
        }
      }
      
      // Health check endpoint for GET requests
      if (request.method === "GET") {
        const url = new URL(request.url);
        if (url.pathname === "/api/health") {
          console.log("[Health] Health check request");
          return corsResponse({
            status: "ok",
            service: "rand-api",
            timestamp: new Date().toISOString()
          });
        }
      }
      
      // Return 404 for any other endpoints or methods
      console.warn(`[404] Endpoint not found: ${request.method} ${new URL(request.url).pathname}`);
      return corsResponse({ 
        success: false, 
        error: "Endpoint not found" 
      }, { status: 404 });
    }
  };
  
  // Helper function to handle CORS preflight requests
  function handleCors(request) {
    // Create a new response with CORS headers
    return new Response(null, {
      status: 204,
      headers: getCorsHeaders(request),
    });
  }
  
  // Helper function to add CORS headers to a response
  function corsResponse(data, options = {}) {
    const headers = options.headers || new Headers();
    
    // Add CORS headers
    Object.entries(getCorsHeaders()).forEach(([key, value]) => {
      headers.set(key, value);
    });
    
    // Add content type for JSON responses
    headers.set('Content-Type', 'application/json');
    
    // Log the response status
    console.log(`[Response] Status: ${options.status || 200}`);
    
    // Return the response with CORS headers
    return Response.json(data, {
      status: options.status || 200,
      headers
    });
  }
  
  // Helper function to get CORS headers
  function getCorsHeaders(request) {
    return {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
      'Access-Control-Max-Age': '86400',
    };
  }